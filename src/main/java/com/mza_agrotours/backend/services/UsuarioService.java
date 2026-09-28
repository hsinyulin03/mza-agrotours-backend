package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.*;
import com.mza_agrotours.backend.entities.*;
import com.mza_agrotours.backend.enums.outbox.TipoOperacion;
import com.mza_agrotours.backend.exceptions.*;
import com.mza_agrotours.backend.mappers.UsuarioMapper;
import com.mza_agrotours.backend.repositories.*;
import com.mza_agrotours.backend.services.outbox.OutboxService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioPersistenceService usuarioPersistenceService;
    private final UsuarioAccesoService usuarioAccesoService;
    private final VisitanteService visitanteService;
    private final FirebaseService firebaseService;


    private final UsuarioRepository usuarioRepository;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;
    private final PaisRepository paisRepository;
    private final VisitanteRepository visitanteRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;
    private final ProductorRepository productorRepository;
    private final UsuarioMapper usuarioMapper;
    private final OutboxService outboxService;

    private final ApplicationEventPublisher publisher;

    public UsuarioService( UsuarioPersistenceService usuarioPersistenceService,
                        UsuarioRepository usuarioRepository,
                        TipoIdentificacionRepository tipoIdentificacionRepository,
                        VisitanteRepository visitanteRepository,
                        UsuarioMapper usuarioMapper,
                        PaisRepository paisRepository,
                        ProductorRepository productorRepository,
                        AdministradorSistemasRepository administradorSistemasRepository,
                        UsuarioAccesoService usuarioAccesoService,
                        VisitanteService visitanteService,
                        OutboxService outboxService,
                           FirebaseService firebaseService,
                           ApplicationEventPublisher publisher) {
        this.usuarioPersistenceService = usuarioPersistenceService;
        this.usuarioRepository = usuarioRepository;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
        this.visitanteRepository = visitanteRepository;
        this.usuarioMapper = usuarioMapper;
        this.paisRepository = paisRepository;
        this.productorRepository = productorRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.usuarioAccesoService = usuarioAccesoService;
        this.visitanteService = visitanteService;
        this.outboxService = outboxService;
        this.firebaseService = firebaseService;
        this.publisher = publisher;
    }

    @Transactional
    public UsuarioGetDTO createUsuario(UsuarioCreateReq usuarioCreateReq, UsuarioAuthDetails usuarioAuthDetails) {
        Optional<Usuario> usuario = usuarioRepository.findByFirebaseUID(usuarioAuthDetails.getFirebaseUID());

        if (usuario.isPresent() && usuario.get().getFechaHoraBaja() == null) {
            throw new AppException(UsuarioError.USUARIO_ALREADY_EXISTS);
        }

        if (usuario.isPresent()) {
            throw new AppException(UsuarioError.USUARIO_INACTIVO);
        }

        TipoIdentificacion tipoIdentificacion = resolveTipoIdentificacion(usuarioCreateReq.getTipoIdentificacion());
        Pais pais = this.paisRepository.findByIso2(usuarioCreateReq.getPaisIso2()).orElseThrow(PaisNotFoundException::new);

        Usuario nuevoUsuario = usuarioMapper.usuarioCreateReqToUsuario(usuarioCreateReq);
        nuevoUsuario.setEmail(usuarioAuthDetails.getEmail());
        nuevoUsuario.setFirebaseUID(usuarioAuthDetails.getFirebaseUID());
        nuevoUsuario.setTipoIdentificacion(tipoIdentificacion);
        nuevoUsuario.setFechaHoraAlta(LocalDateTime.now());

        Visitante visitante = getNewVisitante(nuevoUsuario, pais);

        nuevoUsuario = usuarioRepository.save(nuevoUsuario);
        this.visitanteRepository.save(visitante);

        return this.usuarioMapper.usuarioToUsuarioGetDTO(nuevoUsuario,
                visitante,
                usuarioAccesoService.obtenerAccesosUsuario(nuevoUsuario));
    }


    private TipoIdentificacion resolveTipoIdentificacion(String nombre) {
        final TipoIdentificacionNombre tipoNombre;
        try {
            tipoNombre = TipoIdentificacionNombre.valueOf(nombre.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TipoIdentificacionInvalidoException("Tipo de identificacion invalido: " + nombre);
        }

        return tipoIdentificacionRepository.findByNombre(tipoNombre)
                .orElseThrow(() -> new TipoIdentificacionInvalidoException(
                        "Tipo de identificacion no encontrado: " + nombre));
    }

    @Transactional
    public UsuarioGetDTO getUsuarioByFirebaseUID(String firebaseUID) {
        Usuario usuario = usuarioRepository.findByFirebaseUID(firebaseUID)
                .orElseThrow(() -> new AppException(UsuarioError.USUARIO_NOT_FOUND));

        if (usuario.getFechaHoraBaja() != null) {
            throw new AppException(UsuarioError.USUARIO_INACTIVO);
        }

        Visitante visitante = visitanteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario no tiene un visitante asociado: " + usuario.getId()));

        return this.usuarioMapper.usuarioToUsuarioGetDTO(usuario,
                visitante,
                usuarioAccesoService.obtenerAccesosUsuario(usuario));
    }

    @Transactional
    public UsuarioGetDTO updateUsuarioByEmail(String email, UsuarioUpdateReq usuarioUpdateReq) {
        Usuario usuario = usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new AppException(UsuarioError.USUARIO_NOT_FOUND));

        if (!usuarioUpdateReq.getEmail().equals(usuario.getEmail()) && usuarioRepository.findActiveByEmail(usuarioUpdateReq.getEmail()).isPresent()) {
            throw new UsuarioAlreadyExistsException("Ya existe un usuario con ese email");
        }

        Visitante visitante = this.visitanteRepository.findByUsuario(usuario).orElseThrow(() -> new EntityNotFoundException("No se encontro el visitante del usuario"));
        Pais pais = this.paisRepository.findByIso2(usuarioUpdateReq.getPaisIso2()).orElseThrow(PaisNotFoundException::new);
        TipoIdentificacion tipoIdentificacion = resolveTipoIdentificacion(usuarioUpdateReq.getTipoIdentificacion());

        usuarioMapper.updateUsuarioFromUsuarioUpdateReq(usuario, usuarioUpdateReq);
        usuario.setTipoIdentificacion(tipoIdentificacion);
        visitante.setPais(pais);

        usuario = this.usuarioRepository.save(usuario);
        visitante = this.visitanteRepository.save(visitante);

        return this.usuarioMapper.usuarioToUsuarioGetDTO(usuario,
                visitante,
                usuarioAccesoService.obtenerAccesosUsuario(usuario));
    }


    @Transactional
    public boolean deleteUsuarioByEmail(String email) throws Exception {
        Usuario usuario = usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new AppException(UsuarioError.USUARIO_NOT_FOUND));

        List<CondicionDTO> condicionesEliminacion = getCondicionesDeleteUsuarioHelper(usuario);

        if (!condicionesEliminacion.isEmpty()) {
            throw new UserDeleteConditionNotMetException("No se puede eliminar el usuario", condicionesEliminacion);
        }

        Outbox opEliminar = eliminarUsuarioDeRepositorio(usuario);
        publisher.publishEvent(opEliminar);

        return true;
    }

    private Outbox eliminarUsuarioDeRepositorio(Usuario usuario) {
        usuario.setFechaHoraBaja(LocalDateTime.now());
        usuarioRepository.save(usuario);
        return this.outboxService.crearOutboxPendiente(usuario.getId().toString(), TipoOperacion.ELIMINAR_USUARIO);
    }

    public UsuarioCardDTO getUsuarioCardByEmail(String email) {
        Usuario usuario = usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));
        return usuarioMapper.usuarioToUsuarioCardDTO(usuario);
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

        if (this.visitanteService.tieneReservasActivasByUsuario(usuario)) {
            condiciones.add(
                    new CondicionDTO(
                            "No debés tener reservas pendientes",
                            "Tenés reservas en estado pendiente. Cancelalas o esperá a su resolución para continuar"
                    ));
        }

        // 2. Usuario no es administrador
        if (this.administradorSistemasRepository
                .existsByUsuarioAndFechaHoraBajaIsNull(usuario)) {
            condiciones.add(
                    new CondicionDTO(
                            "Un administrador no puede autoeliminar su cuenta",
                            "Pedí a otro administrador del sistema que gestione la baja de tu cuenta."
                    ));
        }

        // 3. Usuario no es productor lider de un establecimiento vigente
        if (productorRepository.esProductorDeUnEstablecimiento(usuario)) {
            condiciones.add(
                    new CondicionDTO(
                            "Un productor no puede eliminar su cuenta",
                            "Pedí al productor líder de tu establecimiento que gestione la baja de tu cuenta."
                    ));
        }

        return condiciones;
    }

    private Visitante getNewVisitante(Usuario usuario, Pais pais) {
        Visitante visitante = new Visitante();
        visitante.setUsuario(usuario);
        visitante.setPais(pais);
        return visitante;
    }
}
