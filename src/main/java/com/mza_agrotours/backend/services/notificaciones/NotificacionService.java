package com.mza_agrotours.backend.services.notificaciones;

import com.mza_agrotours.backend.events.NotificacionCreadaEvent;
import com.mza_agrotours.backend.dtos.notificacion.NotificacionDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.notificacion.Notificacion;
import com.mza_agrotours.backend.entities.notificacion.TipoNotificacion;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
import com.mza_agrotours.backend.exceptions.NotificacionNotFoundException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.mappers.NotificacionMapper;
import com.mza_agrotours.backend.repositories.NotificacionRepository;
import com.mza_agrotours.backend.repositories.TipoNotificacionRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;
    private final TipoNotificacionRepository tipoNotificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final  NotificacionMapper notificacionMapper;
    private final ApplicationEventPublisher publisher;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               TipoNotificacionRepository tipoNotificacionRepository,
                               UsuarioRepository usuarioRepository,
                               NotificacionMapper notificacionMapper,
                               ApplicationEventPublisher publisher) {
        this.notificacionRepository = notificacionRepository;
        this.tipoNotificacionRepository = tipoNotificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionMapper = notificacionMapper;
        this.publisher = publisher;
    }

    @Transactional
    public void crearNotificacion(Usuario destinatario, TipoNotificacionNombre tipoNotificacionNombre, Establecimiento establecimiento, String enlace, Object... datos) {
        TipoNotificacion tipoNotificacion = this.tipoNotificacionRepository.findByNombre(tipoNotificacionNombre)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de notificación no encontrado en BD: " + tipoNotificacionNombre));

        Notificacion notificacion = new Notificacion();
        notificacion.setDestinatario(destinatario);
        notificacion.setTipoNotificacion(tipoNotificacion);
        notificacion.setTitulo(tipoNotificacionNombre.getTitulo());
        notificacion.setMensaje(String.format(tipoNotificacionNombre.getPlantillaMensaje(), datos));
        notificacion.setUrlLink(enlace);
        notificacion.setFechaHoraAlta(LocalDateTime.now());
        notificacion.setEstablecimiento(establecimiento);

        this.notificacionRepository.save(notificacion);

        this.publisher.publishEvent(new NotificacionCreadaEvent(notificacion.getId()));
    }

    @Transactional(readOnly = true)
    public List<NotificacionDTO> listarNotificaciones(String emailUsuario, UUID establecimientoId) {
        Usuario usuario = obtenerUsuario(emailUsuario);

        List<Notificacion> notificaciones =  this.notificacionRepository.listarNotificaciones(usuario, establecimientoId);

        return this.notificacionMapper.notificacionListToNotificacionDTOList(notificaciones);
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(String emailUsuario, UUID establecimientoId) {
        Usuario usuario = obtenerUsuario(emailUsuario);

        return this.notificacionRepository.contarNoLeidas(usuario, establecimientoId);
    }

    @Transactional
    public NotificacionDTO marcarLeida(UUID id, String emailUsuario, UUID establecimientoId) {
        Usuario usuario = obtenerUsuario(emailUsuario);

        Notificacion notificacion = this.notificacionRepository
                .findNotificacionById(id, usuario, establecimientoId)
                .orElseThrow(() -> new NotificacionNotFoundException());

        if (notificacion.getFechaHoraLectura() == null) {
            notificacion.setFechaHoraLectura(LocalDateTime.now());
            this.notificacionRepository.save(notificacion);
        }
        return this.notificacionMapper.notificacionToNotificacionDTO(notificacion);
    }

    private Usuario obtenerUsuario(String email) {
        return this.usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UsuarioNotFound(email));
    }

}
