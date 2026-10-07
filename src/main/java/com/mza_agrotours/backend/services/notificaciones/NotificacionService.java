package com.mza_agrotours.backend.services.notificaciones;

import com.mza_agrotours.backend.dtos.notificacion.NotificacionDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.notificacion.Notificacion;
import com.mza_agrotours.backend.entities.notificacion.ScopeNotificacion;
import com.mza_agrotours.backend.entities.notificacion.TipoNotificacion;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.ScopeNotificacionNombre;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
import com.mza_agrotours.backend.events.NotificacionCreadaEvent;
import com.mza_agrotours.backend.exceptions.NotificacionNotFoundException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.mappers.NotificacionMapper;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.NotificacionRepository;
import com.mza_agrotours.backend.repositories.ScopeNotificacionRepository;
import com.mza_agrotours.backend.repositories.TipoNotificacionRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;
    private final TipoNotificacionRepository tipoNotificacionRepository;
    private final ScopeNotificacionRepository scopeNotificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;
    private final  NotificacionMapper notificacionMapper;
    private final ApplicationEventPublisher publisher;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               TipoNotificacionRepository tipoNotificacionRepository,
                               ScopeNotificacionRepository scopeNotificacionRepository,
                               UsuarioRepository usuarioRepository,
                               AdministradorSistemasRepository administradorSistemasRepository,
                               NotificacionMapper notificacionMapper,
                               ApplicationEventPublisher publisher) {
        this.notificacionRepository = notificacionRepository;
        this.tipoNotificacionRepository = tipoNotificacionRepository;
        this.scopeNotificacionRepository = scopeNotificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.notificacionMapper = notificacionMapper;
        this.publisher = publisher;
    }

    @Transactional
    public void crearNotificacion(Usuario destinatario, TipoNotificacionNombre tipoNotificacionNombre, Establecimiento establecimiento, String enlace, Object... datos) {
        guardarNotificacion(destinatario, obtenerTipo(tipoNotificacionNombre), establecimiento, enlace, datos);
    }

    /**
     * Una fila por administrador: cada uno lleva su propia lectura y su propio push.
     * Solo reciben los administradores vigentes cuyo rol tiene el permiso indicado.
     */
    @Transactional
    public void notificarAdministradores(PermisoCodigo permiso, TipoNotificacionNombre tipoNotificacionNombre, String enlace, Object... datos) {
        TipoNotificacion tipoNotificacion = obtenerTipo(tipoNotificacionNombre);

        if (tipoNotificacion.getScopeNotificacion().getNombre() != ScopeNotificacionNombre.ADMINISTRADOR) {
            throw new IllegalArgumentException("El tipo " + tipoNotificacionNombre + " no es de scope ADMINISTRADOR");
        }

        for (Usuario administrador : this.administradorSistemasRepository.findUsuariosVigentesConPermiso(permiso)) {
            guardarNotificacion(administrador, tipoNotificacion, null, enlace, datos);
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificacionDTO> listarNotificaciones(String emailUsuario, ScopeNotificacionNombre scope, UUID establecimientoId, Pageable pageable) {
        Usuario usuario = obtenerUsuario(emailUsuario);

        Page<Notificacion> notificaciones =  this.notificacionRepository
                .listarNotificaciones(usuario, obtenerScope(scope, establecimientoId), establecimientoId, pageable);

        return notificaciones.map(this.notificacionMapper::notificacionToNotificacionDTO);
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(String emailUsuario, ScopeNotificacionNombre scope, UUID establecimientoId) {
        Usuario usuario = obtenerUsuario(emailUsuario);

        return this.notificacionRepository.contarNoLeidas(usuario, obtenerScope(scope, establecimientoId), establecimientoId);
    }

    @Transactional
    public NotificacionDTO marcarLeida(UUID id, String emailUsuario, ScopeNotificacionNombre scope, UUID establecimientoId) {
        Usuario usuario = obtenerUsuario(emailUsuario);

        Notificacion notificacion = this.notificacionRepository
                .findNotificacionById(id, usuario, obtenerScope(scope, establecimientoId), establecimientoId)
                .orElseThrow(() -> new NotificacionNotFoundException());

        marcarNotificacionLeidaEn(notificacion, LocalDateTime.now());

        return this.notificacionMapper.notificacionToNotificacionDTO(notificacion);
    }

    @Transactional
    public int marcarLeidasListado(LocalDateTime localDateTime, String emailUsuario, ScopeNotificacionNombre scope, UUID establecimientoId) {
        Usuario usuario = obtenerUsuario(emailUsuario);
        List<Notificacion> notificaciones = this.notificacionRepository
                .findAllNoLeidasHastaFechaByDestinatarioAndEstablecimientoId(
                        localDateTime, usuario, obtenerScope(scope, establecimientoId), establecimientoId);

        LocalDateTime fechaHoraLectura = LocalDateTime.now();

        for (Notificacion notificacion : notificaciones) {
            marcarNotificacionLeidaEn(notificacion, fechaHoraLectura);
        }

        this.notificacionRepository.saveAll(notificaciones);

        return notificaciones.size();
    }

    private void guardarNotificacion(Usuario destinatario, TipoNotificacion tipoNotificacion, Establecimiento establecimiento, String enlace, Object... datos) {
        validarEstablecimiento(tipoNotificacion.getScopeNotificacion().getNombre(), establecimiento);

        TipoNotificacionNombre tipoNotificacionNombre = tipoNotificacion.getNombre();

        Notificacion notificacion = new Notificacion();
        notificacion.setDestinatario(destinatario);
        notificacion.setTipoNotificacion(tipoNotificacion);
        notificacion.setScopeNotificacion(tipoNotificacion.getScopeNotificacion());
        notificacion.setTitulo(tipoNotificacionNombre.getTitulo());
        notificacion.setMensaje(String.format(tipoNotificacionNombre.getPlantillaMensaje(), datos));
        notificacion.setUrlLink(enlace);
        notificacion.setFechaHoraAlta(LocalDateTime.now());
        notificacion.setEstablecimiento(establecimiento);

        this.notificacionRepository.save(notificacion);

        this.publisher.publishEvent(new NotificacionCreadaEvent(notificacion.getId()));
    }

    private Notificacion marcarNotificacionLeidaEn(Notificacion notificacion, LocalDateTime fechaHoraLectura) {
        if (notificacion.getFechaHoraLectura() == null) {
            notificacion.setFechaHoraLectura(fechaHoraLectura);
        }
        return notificacion;
    }

    private TipoNotificacion obtenerTipo(TipoNotificacionNombre nombre) {
        return this.tipoNotificacionRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de notificación no encontrado en BD: " + nombre));
    }

    private ScopeNotificacion obtenerScope(ScopeNotificacionNombre nombre, UUID establecimientoId) {
        validarEstablecimiento(nombre, establecimientoId);

        return this.scopeNotificacionRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Scope de notificación no encontrado en BD: " + nombre));
    }

    private void validarEstablecimiento(ScopeNotificacionNombre scope, Object establecimiento) {
        boolean requiereEstablecimiento = scope == ScopeNotificacionNombre.ESTABLECIMIENTO;

        if (requiereEstablecimiento != (establecimiento != null)) {
            throw new IllegalArgumentException("Una notificación de scope " + scope
                    + (requiereEstablecimiento ? " requiere" : " no admite") + " establecimiento");
        }
    }

    private Usuario obtenerUsuario(String email) {
        return this.usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UsuarioNotFound(email));
    }

}
