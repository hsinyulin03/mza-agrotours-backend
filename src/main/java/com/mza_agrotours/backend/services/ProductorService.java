package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.productor.ProductorCreateReq;
import com.mza_agrotours.backend.dtos.productor.ProductorGetDTO;
import com.mza_agrotours.backend.dtos.productor.ProductorUpdateReq;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.productor.EstadoProductor;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.ProductorError;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.mappers.ProductorMapper;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.EstadoProductorRepository;
import com.mza_agrotours.backend.repositories.ProductorRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import com.mza_agrotours.backend.services.roles_permisos.RolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductorService {
    private static final Logger log = LoggerFactory.getLogger(ProductorService.class);

    private static final String MOTIVO_ALTA_LIDER = "Creacion productor lider";
    private static final String MOTIVO_ALTA = "Alta de productor";
    private static final String MOTIVO_BAJA = "Baja de productor";
    private static final String MOTIVO_FIN_SUSPENSION_AUTOMATICA = "Fin automatico de la suspension";
    private static final String MOTIVO_FIN_SUSPENSION_ANTICIPADA = "Levantamiento anticipado de la suspension";

    private final EstadoProductorRepository estadoProductorRepository;
    private final RolService rolService;
    private final ProductorRepository productorRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final RolRepository rolRepository;
    private final ProductorMapper productorMapper;
    private final ProductorService self;

    public ProductorService(EstadoProductorRepository estadoProductorRepository,
                            RolService rolService,
                            ProductorRepository productorRepository,
                            UsuarioRepository usuarioRepository,
                            EstablecimientoRepository establecimientoRepository,
                            RolRepository rolRepository,
                            ProductorMapper productorMapper,
                            @Lazy ProductorService self) {
        this.estadoProductorRepository = estadoProductorRepository;
        this.rolService = rolService;
        this.productorRepository = productorRepository;
        this.usuarioRepository = usuarioRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.rolRepository = rolRepository;
        this.productorMapper = productorMapper;
        this.self = self;
    }

    // ---------------------------------------------------------------- ABM

    @Transactional(readOnly = true)
    public List<ProductorGetDTO> findProductoresVigentes(UUID establecimientoId) {
        List<Productor> productores = this.productorRepository.findVigentesByEstablecimiento(establecimientoId);
        return this.productorMapper.productorListToProductorGetDTOList(productores);
    }

    @Transactional
    public ProductorGetDTO crearProductor(UUID establecimientoId, ProductorCreateReq productorCreateReq) {
        Establecimiento establecimiento = this.establecimientoRepository
                .findByIdAndFechaHoraBajaIsNull(establecimientoId)
                .orElseThrow(() -> new AppException(ProductorError.ESTABLECIMIENTO_NOT_FOUND));

        Usuario usuario = this.usuarioRepository
                .findActiveByEmail(productorCreateReq.getEmailUsuario())
                .orElseThrow(() -> new UsuarioNotFound("No se encontro el usuario"));

        if (this.productorRepository
                .existsByUsuarioAndEstablecimiento_IdAndFechaHoraBajaIsNull(usuario, establecimientoId)) {
            throw new AppException(ProductorError.ALREADY_EXISTS);
        }

        Rol rol = obtenerRolAsignable(productorCreateReq.getRolId(), establecimientoId);
        EstadoProductor estadoActivo = obtenerEstadoProductorByNombre(EstadoProductorNombre.ACTIVO);

        LocalDateTime ahora = LocalDateTime.now();
        Productor productor = new Productor();
        productor.setFechaHoraAlta(ahora);
        productor.setEstablecimiento(establecimiento);
        productor.setUsuario(usuario);
        productor.setRol(rol);
        productor.cambiarEstado(estadoActivo, MOTIVO_ALTA, ahora, null);

        productor = this.productorRepository.save(productor);

        // TODO: entidad que diga quien hizo el cambio
        return this.productorMapper.productorToProductorGetDTO(productor);
    }

    @Transactional
    public ProductorGetDTO modificarRolProductor(UUID establecimientoId,
                                                 UUID productorId,
                                                 ProductorUpdateReq productorUpdateReq,
                                                 String emailEjecutor) {
        Productor productor = obtenerProductorEnEstablecimiento(productorId, establecimientoId);

        validarNoEsAutoGestion(productor, emailEjecutor);
        validarNoEsLider(productor);

        // Mismas reglas que en el alta: solo roles de productor vigentes,
        // del propio establecimiento y no protegidos.
        productor.setRol(obtenerRolAsignable(productorUpdateReq.getRolId(), establecimientoId));
        productor = this.productorRepository.save(productor);

        // TODO: entidad que diga quien hizo el cambio
        return this.productorMapper.productorToProductorGetDTO(productor);
    }

    @Transactional
    public boolean bajaProductor(UUID establecimientoId, UUID productorId, String emailEjecutor) {
        Productor productor = obtenerProductorEnEstablecimiento(productorId, establecimientoId);

        validarNoEsAutoGestion(productor, emailEjecutor);
        validarNoEsLider(productor);

        LocalDateTime ahora = LocalDateTime.now();
        productor.setFechaHoraBaja(ahora);
        productor.cambiarEstado(obtenerEstadoProductorByNombre(EstadoProductorNombre.BAJA),
                MOTIVO_BAJA, ahora, null);

        // TODO: entidad que diga quien hizo el cambio
        this.productorRepository.save(productor);
        return true;
    }

    // -------------------------------------------------------- Suspensiones

    /**
     * Suspende al productor hasta una fecha planificada. El vencimiento queda en el tramo
     * de estado como fechaHoraFinPrevista; el tramo sigue abierto (fechaHoraFin == null)
     * hasta que se lo levante, sea por vencimiento o de forma anticipada.
     */
    @Transactional
    public ProductorGetDTO suspenderProductor(UUID establecimientoId,
                                              UUID productorId,
                                              String motivo,
                                              LocalDateTime fechaHoraFinPrevista,
                                              String emailEjecutor) {
        Productor productor = obtenerProductorEnEstablecimiento(productorId, establecimientoId);

        validarNoEsAutoGestion(productor, emailEjecutor);
        validarNoEsLider(productor);

        if (motivo == null || motivo.isBlank()) {
            throw new AppException(ProductorError.MOTIVO_REQUERIDO);
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (fechaHoraFinPrevista == null || !fechaHoraFinPrevista.isAfter(ahora)) {
            throw new AppException(ProductorError.FECHA_FIN_SUSPENSION_INVALIDA);
        }

        EstadoProductorNombre estadoActual = productor.getEstadoActual().getNombre();
        if (estadoActual == EstadoProductorNombre.BAJA) {
            throw new AppException(ProductorError.SUSPENSION_SOBRE_BAJA);
        }
        if (estadoActual == EstadoProductorNombre.LICENCIA) {
            throw new AppException(ProductorError.YA_SUSPENDIDO);
        }

        EstadoProductor estadoSuspendido = obtenerEstadoProductorByNombre(EstadoProductorNombre.LICENCIA);
        productor.cambiarEstado(estadoSuspendido, motivo, ahora, fechaHoraFinPrevista);

        productor = this.productorRepository.save(productor);
        return this.productorMapper.productorToProductorGetDTO(productor);
    }

    /**
     * Levanta la suspension antes de su vencimiento. No necesita tocar la fecha prevista:
     * al cerrar el tramo con fechaHoraFin, deja de ser candidato para el scheduler.
     */
    @Transactional
    public ProductorGetDTO levantarSuspension(UUID establecimientoId,
                                              UUID productorId,
                                              String motivo,
                                              String emailEjecutor) {
        Productor productor = obtenerProductorEnEstablecimiento(productorId, establecimientoId);

        validarNoEsAutoGestion(productor, emailEjecutor);

        if (productor.getEstadoActual().getNombre() != EstadoProductorNombre.LICENCIA) {
            throw new AppException(ProductorError.NO_SUSPENDIDO);
        }

        Productor reactivado = reactivar(productor,
                motivo != null && !motivo.isBlank() ? motivo : MOTIVO_FIN_SUSPENSION_ANTICIPADA,
                LocalDateTime.now());
        return this.productorMapper.productorToProductorGetDTO(reactivado);
    }

    /**
     * Punto de entrada del scheduler: reactiva a todos los productores cuya suspension vencio.
     */
    @Transactional(readOnly = true)
    public void levantarSuspensionesVencidas() {
        LocalDateTime ahora = LocalDateTime.now();
        List<UUID> idsVencidos = this.productorRepository.findIdsConSuspensionVencida(ahora);
        if (idsVencidos.isEmpty()) {
            return;
        }

        List<String> idFallidos = new ArrayList<>();
        for (UUID productorId : idsVencidos) {
            try {
                self.reactivarPorVencimiento(productorId, ahora);
            } catch (Exception e) {
                idFallidos.add(productorId.toString());
                log.warn("No se pudo levantar la suspension del productor {}", productorId, e);
            }
        }

        log.info("Se levantaron {}/{} suspensiones vencidas. Fallaron {}, con ids: {}",
                idsVencidos.size() - idFallidos.size(), idsVencidos.size(), idFallidos.size(), idFallidos);
    }

    /**
     * Reactiva un unico productor en su propia transaccion, para que un fallo al guardar
     * uno no revierta las reactivaciones ya confirmadas de los demas.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reactivarPorVencimiento(UUID productorId, LocalDateTime ahora) {
        Productor productor = this.productorRepository
                .findByIdAndFechaHoraBajaIsNull(productorId)
                .orElseThrow(() -> new AppException(ProductorError.NOT_FOUND));

        // Relectura defensiva: entre la query del scheduler y esta transaccion la suspension
        // pudo haberse levantado a mano, y en ese caso no hay nada que hacer.
        if (productor.getEstadoActual().getNombre() != EstadoProductorNombre.LICENCIA) {
            return;
        }

        reactivar(productor, MOTIVO_FIN_SUSPENSION_AUTOMATICA, ahora);
    }

    // ------------------------------------------------------------- Interno

    @Transactional
    public Productor crearProductorLider(Usuario usuarioProductor,
                                         Establecimiento establecimiento) {
        EstadoProductor estadoActivo = obtenerEstadoProductorByNombre(EstadoProductorNombre.ACTIVO);

        LocalDateTime ahora = LocalDateTime.now();
        Productor productorLider = new Productor();
        productorLider.setFechaHoraAlta(ahora);
        productorLider.setEstablecimiento(establecimiento);
        productorLider.setUsuario(usuarioProductor);
        productorLider.setRol(this.rolService.crearRolProductorLider(establecimiento));
        productorLider.cambiarEstado(estadoActivo, MOTIVO_ALTA_LIDER, ahora, null);

        return this.productorRepository.save(productorLider);
    }

    private Productor reactivar(Productor productor, String motivo, LocalDateTime ahora) {
        EstadoProductor estadoActivo = obtenerEstadoProductorByNombre(EstadoProductorNombre.ACTIVO);
        productor.cambiarEstado(estadoActivo, motivo, ahora, null);
        return this.productorRepository.save(productor);
    }

    private Productor obtenerProductorEnEstablecimiento(UUID productorId, UUID establecimientoId) {
        return this.productorRepository
                .findByIdAndEstablecimiento_IdAndFechaHoraBajaIsNull(productorId, establecimientoId)
                .orElseThrow(() -> new AppException(ProductorError.NOT_FOUND));
    }

    /**
     * Solo son asignables los roles de productor vigentes del propio establecimiento
     * y no protegidos: el rol de Productor Lider no se reparte por ABM.
     */
    private Rol obtenerRolAsignable(UUID rolId, UUID establecimientoId) {
        return this.rolRepository
                .findVigenteMutableByIdScoped(rolId, TipoPermisoNombre.PRODUCTOR, establecimientoId)
                .orElseThrow(() -> new AppException(ProductorError.ROL_INVALIDO));
    }

    // El Productor Lider es el rol protegido del establecimiento: no se le cambia el rol
    // ni se lo da de baja por ABM.
    private void validarNoEsLider(Productor productor) {
        if (Boolean.TRUE.equals(productor.getRol().getEsProtegido())) {
            throw new AppException(ProductorError.LIDER_INMUTABLE);
        }
    }

    // Nadie gestiona su propia participacion: ni para escalar su rol ni para darse de baja
    private void validarNoEsAutoGestion(Productor productor, String emailEjecutor) {
        if (productor.getUsuario().getEmail().equalsIgnoreCase(emailEjecutor)) {
            throw new AppException(ProductorError.AUTO_GESTION_PROHIBIDA);
        }
    }

    private EstadoProductor obtenerEstadoProductorByNombre(EstadoProductorNombre estadoNombre) {
        return this.estadoProductorRepository
                .findByNombreAndFechaHoraBajaIsNull(estadoNombre)
                .orElseThrow(() -> new AppException(ProductorError.ESTADO_NO_CONFIGURADO));
    }
}
