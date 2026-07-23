package com.mza_agrotours.backend.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.mza_agrotours.backend.dtos.CondicionDTO;
import com.mza_agrotours.backend.dtos.UsuarioCreateReq;
import com.mza_agrotours.backend.dtos.UsuarioGetDTO;
import com.mza_agrotours.backend.dtos.UsuarioUpdateReq;
import com.mza_agrotours.backend.entities.*;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.exceptions.*;
import com.mza_agrotours.backend.mappers.UsuarioMapper;
import com.mza_agrotours.backend.repositories.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioPersistenceService usuarioPersistenceService;

    private final UsuarioRepository usuarioRepository;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;
    private final PaisRepository paisRepository;
    private final VisitanteRepository visitanteRepository;
    private final ReservaRepository reservaRepository;
    private final EstadoReservaRepository estadoReservaRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;

    private final UsuarioMapper usuarioMapper;

    public UsuarioService( UsuarioPersistenceService usuarioPersistenceService,
                        UsuarioRepository usuarioRepository,
                        TipoIdentificacionRepository tipoIdentificacionRepository,
                        VisitanteRepository visitanteRepository,
                        UsuarioMapper usuarioMapper,
                        PaisRepository paisRepository,
                        ReservaRepository reservaRepository,
                        EstadoReservaRepository estadoReservaRepository,
                        AdministradorSistemasRepository administradorSistemasRepository) {
        this.usuarioPersistenceService = usuarioPersistenceService;
        this.usuarioRepository = usuarioRepository;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
        this.visitanteRepository = visitanteRepository;
        this.usuarioMapper = usuarioMapper;
        this.paisRepository = paisRepository;
        this.reservaRepository = reservaRepository;
        this.estadoReservaRepository = estadoReservaRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
    }

    public UsuarioGetDTO createUsuario(UsuarioCreateReq usuarioCreateReq) throws Exception {
        UserRecord record = null;
        try {
            usuarioCreateReq.setEmail(usuarioCreateReq.getEmail().trim());

            if (usuarioRepository.findActiveByEmail(usuarioCreateReq.getEmail()).isPresent()) {
                throw new UsuarioAlreadyExistsException("Ya existe un usuario con ese email");
            }

            TipoIdentificacion tipoIdentificacion = resolveTipoIdentificacion(usuarioCreateReq.getTipoIdentificacion());
            Pais pais = this.paisRepository.findByIso2(usuarioCreateReq.getPaisIso2()).orElseThrow(PaisNotFoundException::new);

            record = FirebaseAuth.getInstance().createUser(usuarioMapper.usuarioCreateReqToFirebaseCreateRequest(usuarioCreateReq));

            Usuario nuevoUsuario = usuarioMapper.usuarioCreateReqToUsuario(usuarioCreateReq);
            nuevoUsuario.setFirebaseUID(record.getUid());
            nuevoUsuario.setTipoIdentificacion(tipoIdentificacion);
            Usuario usuario = usuarioPersistenceService.saveUsuarioConVisitante(nuevoUsuario, pais);

            return usuarioMapper.usuarioToUsuarioGetDTO(usuario);
        } catch (Exception e) {
            if (record != null) {
                try {
                    FirebaseAuth.getInstance().deleteUser(record.getUid());
                } catch (FirebaseAuthException firebaseAuthException) {
                    // TODO: hacer un script que elimine los usuarios que no se han podido crear
                    log.error(
                            "USUARIO HUERFANO: se creo el usuario en Firebase con UID={} y email={} " +
                                    "pero fallo el guardado en la base de datos Y ADEMAS fallo el borrado " +
                                    "compensatorio en Firebase. Requiere limpieza manual/reconciliacion.",
                            record.getUid(),
                            usuarioCreateReq.getEmail(),
                            firebaseAuthException
                    );
                }
            }

            throw e;
        }
    }


    private TipoIdentificacion resolveTipoIdentificacion(String nombre) {
        final TipoIdentificacionNombre tipoNombre;
        try {
            tipoNombre = TipoIdentificacionNombre.valueOf(nombre);
        } catch (IllegalArgumentException e) {
            throw new TipoIdentificacionInvalidoException("Tipo de identificacion invalido: " + nombre);
        }

        return tipoIdentificacionRepository.findByNombre(tipoNombre)
                .orElseThrow(() -> new TipoIdentificacionInvalidoException(
                        "Tipo de identificacion no encontrado: " + nombre));
    }

    @Transactional
    public UsuarioGetDTO getUsuarioByEmail(String email) {
        String normalizedEmail = email == null ? null : email.trim();
        Usuario usuario = usuarioRepository.findActiveByEmail(normalizedEmail)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        UsuarioGetDTO usuarioGetDTO = usuarioMapper.usuarioToUsuarioGetDTO(usuario);
        Visitante visitante = visitanteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario no tiene un visitante asociado: " + usuario.getId()));
        usuarioGetDTO.setPaisIso2(visitante.getPais().getIso2());

        return usuarioGetDTO;
    }

    public UsuarioGetDTO updateUsuarioByEmail(String email, UsuarioUpdateReq usuarioUpdateReq) throws Exception {
        Usuario usuario = usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        if (!usuarioUpdateReq.getEmail().equals(usuario.getEmail()) && usuarioRepository.findActiveByEmail(usuarioUpdateReq.getEmail()).isPresent()) {
            throw new UsuarioAlreadyExistsException("Ya existe un usuario con ese email");
        }

        Visitante visitante = this.visitanteRepository.findByUsuario(usuario).orElseThrow(IllegalStateException::new);
        Pais pais = this.paisRepository.findByIso2(usuarioUpdateReq.getPaisIso2()).orElseThrow(PaisNotFoundException::new);
        TipoIdentificacion tipoIdentificacion = resolveTipoIdentificacion(usuarioUpdateReq.getTipoIdentificacion());

        usuarioMapper.updateUsuarioFromUsuarioUpdateReq(usuario, usuarioUpdateReq);
        usuario.setTipoIdentificacion(tipoIdentificacion);
        visitante.setPais(pais);

        // Firebase primero: si falla, se lanza la excepcion y no se persiste nada en la base de datos.
        UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(usuario.getFirebaseUID());
        updateRequest.setEmail(usuarioUpdateReq.getEmail());
        updateRequest.setDisplayName(usuarioUpdateReq.getNombre());
        updateRequest.setPhoneNumber(usuarioUpdateReq.getTelefono());

        FirebaseAuth.getInstance().updateUser(updateRequest);

        // Firebase ya se actualizo. Si la persistencia en la base de datos falla, Firebase y la base
        // de datos quedan desincronizados: por ahora solo lo registramos para reconciliacion manual.
        try {
            usuario = usuarioPersistenceService.updateUsuarioConVisitante(usuario, visitante);
        } catch (Exception e) {
            log.error(
                    "USUARIO INCONSISTENTE: se actualizo el usuario en Firebase con UID={} (email nuevo={}) " +
                            "pero fallo la actualizacion en la base de datos. Firebase y la base de datos " +
                            "quedaron desincronizados. Requiere limpieza manual/reconciliacion.",
                    usuario.getFirebaseUID(),
                    usuarioUpdateReq.getEmail(),
                    e
            );
        }

        return usuarioMapper.usuarioToUsuarioGetDTO(usuario);
    }

    public boolean deleteUsuarioByEmail(String email) throws Exception {
        Usuario usuario = usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        List<CondicionDTO> condicionesEliminacion = getCondicionesDeleteUsuarioHelper(usuario);

        if (!condicionesEliminacion.isEmpty()) {
            throw new UserDeleteConditionNotMetException("No se puede eliminar el usuario", condicionesEliminacion);
        }

        // Firebase primero: si falla, se lanza la excepcion y no se toca la base de datos.
        FirebaseAuth.getInstance().deleteUser(usuario.getFirebaseUID());

        // Firebase ya elimino el usuario (ya no puede autenticarse). Si la baja en la base de datos
        // falla, el usuario sigue activo en la base pero sin acceso: por ahora solo lo registramos.
        try {
            usuarioPersistenceService.softDeleteUsuario(usuario);
        } catch (Exception e) {
            log.error(
                    "USUARIO INCONSISTENTE: se elimino el usuario en Firebase con UID={} y email={} " +
                            "pero fallo la baja en la base de datos. El usuario ya no puede autenticarse " +
                            "pero sigue activo en la base de datos. Requiere limpieza manual/reconciliacion.",
                    usuario.getFirebaseUID(),
                    email,
                    e
            );
        }

        return true;
    }

    @Transactional
    public List<CondicionDTO> getCondicionesDeleteUsuario(String email) throws Exception {
        Usuario usuario = usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        return getCondicionesDeleteUsuarioHelper(usuario);
    }

    private List<CondicionDTO> getCondicionesDeleteUsuarioHelper(Usuario usuario) throws Exception {
       // 1. Usuario no tiene reservas activas
        List<CondicionDTO> condiciones = new ArrayList<>();
        Visitante visitante = visitanteRepository.findByUsuario(usuario).orElseThrow(IllegalStateException::new);
        EstadoReserva estadoPendiente = estadoReservaRepository.findByNombre(EstadoReservaNombre.PENDIENTE)
                .orElseThrow(() -> new Exception("Estado de reserva no encontrado"));

        List<Reserva> reservasActivasList = reservaRepository.findByVisitanteAndReservaEstadoActual(visitante.getId(), estadoPendiente.getId());

        if (!reservasActivasList.isEmpty()) {
            condiciones.add(
                    new CondicionDTO(
                            "reservasActivas",
                            "El usuario tiene reservas activas"
                    ));
        }

        // 2. Usuario no es administrador (TODO)
        Optional<AdministradorSistemas> administradorSistemasOptional = administradorSistemasRepository.findByUsuarioId(usuario.getId());
        if (administradorSistemasOptional.isPresent()) {
            condiciones.add(
                    new CondicionDTO(
                            "administradorSistemas",
                            "El usuario es administrador"
                    ));
        }

        // 3. Usuario no es productor lider de un establecimiento vigente (TODO)

        return condiciones;
    }
}
