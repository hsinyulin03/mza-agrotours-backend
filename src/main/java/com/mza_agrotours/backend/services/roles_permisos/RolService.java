package com.mza_agrotours.backend.services.roles_permisos;

import com.mza_agrotours.backend.dtos.roles_permisos.*;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.RolProtegido;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.RolError;
import com.mza_agrotours.backend.mappers.RolMapper;
import com.mza_agrotours.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RolService {
    private final RolRepository rolRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;
    private final RolMapper rolMapper;
    private final TipoPermisoRepository tipoPermisoRepository;
    private final PermisoRepository permisoRepository;
    private final ProductorRepository productorRepository;
    private final EstablecimientoRepository establecimientoRepository;

    public RolService(RolRepository rolRepository,
                      AdministradorSistemasRepository administradorSistemasRepository,
                      RolMapper rolMapper,
                      TipoPermisoRepository tipoPermisoRepository,
                      PermisoRepository permisoRepository,
                      ProductorRepository productorRepository,
                      EstablecimientoRepository establecimientoRepository) {
        this.rolRepository = rolRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.rolMapper = rolMapper;
        this.tipoPermisoRepository = tipoPermisoRepository;
        this.permisoRepository = permisoRepository;
        this.productorRepository = productorRepository;
        this.establecimientoRepository = establecimientoRepository;
    }

    @Transactional(readOnly = true)
    public List<PermisoCodigo> obtenerPermisoCodigosAdminPorEmail(String email) {
        return this.administradorSistemasRepository.findPermisoCodigosByEmailActivo(email);
    }

    @Transactional(readOnly = true)
    public List<RolGetCatalogoDTO> obtenerRolesAdminCatalogo() {
        return this.obtenerRolesCatalogoByTipoPermisoNombre(RolScope.admin());
    }

    @Transactional
    public RolCreateResponse crearRolAdmin(RolCreateRequest rolCreateRequest) {
        Rol rolCreado = crearRol(rolCreateRequest, RolScope.admin());
        return this.rolMapper.rolToRolCreateResponse(rolCreado);
    }

    @Transactional
    public RolUpdateResponse modificarRolAdmin(String rolId, RolUpdateRequest rolUpdateRequest) {
        Rol rolModificado = modificarRol(rolId, rolUpdateRequest, RolScope.admin());
        return this.rolMapper.rolToRolUpdateResponse(rolModificado);
    }

    @Transactional
    public boolean bajaRolAdmin(String rolId) {
        return bajaRol(rolId, RolScope.admin());
    }

    /**
     * Crea el rol lider de un establecimiento: protegido y con todos los permisos del tipo
     * PRODUCTOR. Cada establecimiento tiene el suyo, porque el rol no queda definido solo por
     * sus permisos sino tambien por su scope, asi que dos establecimientos no pueden compartir
     * uno aunque los permisos sean identicos.
     *
     * <p>No pasa por {@link #crearRol}: ese camino nace de un {@link RolCreateRequest} de la API
     * y fuerza {@code esProtegido = false}.
     *
     * @param establecimiento establecimiento ya persistido al que se le asocia el rol.
     */
    @Transactional
    public Rol crearRolProductorLider(Establecimiento establecimiento) {
        TipoPermiso tipoPermiso = this.obtenerTipoPermiso(TipoPermisoNombre.PRODUCTOR);
        RolScopeSolved rolScopeSolved = new RolScopeSolved(tipoPermiso, establecimiento);

        Rol rolLider = new Rol();
        rolLider.setNombre(RolProtegido.PRODUCTOR_LIDER.getNombre());
        rolLider.setDescripcion("Rol productor con todos los permisos");
        rolLider.setEsProtegido(true);
        rolLider.setTipoPermiso(rolScopeSolved.tipoPermiso());
        rolLider.setEstablecimiento(rolScopeSolved.establecimiento());
        rolLider.setPermisos(new ArrayList<>(
                this.permisoRepository.findByTipoPermiso(rolScopeSolved.tipoPermiso())));

        return this.rolRepository.save(rolLider);
    }

    //Gestión de roles de productor
    @Transactional(readOnly = true)
    public List<RolGetCatalogoDTO> obtenerRolesProductorCatalogo(UUID establecimientoId) {
        return this.obtenerRolesCatalogoByTipoPermisoNombre(RolScope.productor(establecimientoId));
    }

    @Transactional
    public RolCreateResponse crearRolProductor(UUID establecimientoId, RolCreateRequest rolCreateRequest) {
        Rol rolCreado = crearRol(rolCreateRequest, RolScope.productor(establecimientoId));
        return this.rolMapper.rolToRolCreateResponse(rolCreado);
    }

    @Transactional
    public RolUpdateResponse modificarRolProductor(UUID establecimientoId, String rolId, RolUpdateRequest rolUpdateRequest) {
        Rol rolModificado = modificarRol(rolId, rolUpdateRequest, RolScope.productor(establecimientoId));
        return this.rolMapper.rolToRolUpdateResponse(rolModificado);
    }

    @Transactional
    public boolean bajaRolProductor(UUID establecimientoId, String rolId) {
        return bajaRol(rolId, RolScope.productor(establecimientoId));
    }

    private Rol crearRol(RolCreateRequest rolCreateRequest, RolScope scope) {
        RolScopeSolved rolScopeSolved = this.resolverRolScope(scope);

        if (this.rolRepository.existsByNombreAndTipoPermisoAndEstablecimiento(rolCreateRequest.getNombre(), scope.tipoPermisoNombre(), scope.establecimientoId())) {
            throw new AppException(RolError.ROL_ALREADY_EXISTS);
        }

        List<Permiso> permisos = this.resolvePermisos(rolScopeSolved.tipoPermiso(), rolCreateRequest.getPermisos());

        Rol nuevoRol = this.rolMapper
                .rolScopeSolvedAndCreateRequestAndPermisosToRol(rolCreateRequest, rolScopeSolved, permisos);

        return this.rolRepository.save(nuevoRol);
    }

    private Rol modificarRol(String rolId, RolUpdateRequest rolUpdateRequest, RolScope scope) {
        RolScopeSolved rolScopeSolved = this.resolverRolScope(scope);
        /** TODO para pensar
         * Actualmente se permite que el administrador modifique los
         * permisos para su propio rol, algo que podría ser un tiro
         * en el pié, creo que lo voy a evitar.
         *
         * Pero cualquier cosa, puede suceder tranquilamente el muchcacho
         * ponerse de acuerdo con otro y elevar el rol del otro, después el otro
         * el de él y así pueden adquirir más permisos de los que debería. Lo
         * que practicamente hacer que esto de gestionar roles sea inútil.
         *
         * Hay varias soluciones (enumerados arbitrariamente):
         * 1. Hacerselo un poco más difícil evitando que pueda modificar su propio rol
         * 2. Que sólo pueda asignar los permisos que él tenga
         * 3. Que solo el ADMIN_LIDER tenga permisos para gestionar los roles
         */


        Rol rol = this.rolRepository
                .findVigenteMutableByIdScoped(
                        UUID.fromString(rolId),
                        scope.tipoPermisoNombre(),
                        scope.establecimientoId())
                .orElseThrow(() -> new AppException(RolError.NOT_FOUND));

        List<Permiso> permisos = this.resolvePermisos(rolScopeSolved.tipoPermiso(), rolUpdateRequest.getPermisos());

        rol.setNombre(rolUpdateRequest.getNombre());
        rol.setDescripcion(rolUpdateRequest.getDescripcion());
        rol.setPermisos(permisos);
        return this.rolRepository.save(rol);
    }

    private boolean bajaRol(String rolId, RolScope scope) {
        Rol rol = this.rolRepository
                .findVigenteMutableByIdScoped(
                        UUID.fromString(rolId),
                        scope.tipoPermisoNombre(),
                        scope.establecimientoId())
                .orElseThrow(() -> new AppException(RolError.NOT_FOUND));


        Integer cantidadUsers = obtenerCantidadUsuariosPorRol(rol);
        if (cantidadUsers > 0) {
            throw new AppException(RolError.BAJA_ROL_CON_USUARIOS);
        }

        rol.setFechaHoraBaja(LocalDateTime.now());
        this.rolRepository.save(rol);
        return true;
    }

    private List<RolGetCatalogoDTO> obtenerRolesCatalogoByTipoPermisoNombre(RolScope scope) {
        //TODO: te lista los roles vigentes, pero en las pantallas veo que también te muestra los dado de baja
        List<Rol> rolesByTipoPermiso = this.rolRepository
                .findVigentesEnScope(scope.tipoPermisoNombre(), scope.establecimientoId());

        List<RolGetCatalogoDTO> rolesCatalogo = new ArrayList<>();
        for (Rol rol : rolesByTipoPermiso) {
            RolGetCatalogoDTO rolGetCatalogoDTO = this.rolMapper.rolToRolGetCatalogoDTO(rol);

            Integer cantidadUsers = obtenerCantidadUsuariosPorRol(rol);
            rolGetCatalogoDTO.setCantidadUsuarios(cantidadUsers);

            rolesCatalogo.add(rolGetCatalogoDTO);
        }

        return rolesCatalogo;
    }

    private Integer obtenerCantidadUsuariosPorRol(Rol rol) {
        TipoPermisoNombre tipoPermisoNombre = rol.getTipoPermiso().getNombre();

        return switch (tipoPermisoNombre) {
            case ADMIN -> this.administradorSistemasRepository.countByRolAndFechaHoraBajaIsNull(rol);
            case PRODUCTOR -> this.productorRepository.countByRolAndFechaHoraBajaIsNull(rol);
            default -> throw new IllegalStateException("Unexpected value: " + tipoPermisoNombre);
        };
    }

    private List<Permiso> resolvePermisos(TipoPermiso tipoPermiso, List<String> permisos) {
        List<Permiso> permisosFetched = this.permisoRepository.findByTipoPermisoAndCodigoIn(tipoPermiso, permisos);
        if (permisosFetched.size() != permisos.size()) {
            throw new AppException(RolError.PERMISO_INVALIDO);
        }
        return permisosFetched;
    }

    private RolScopeSolved resolverRolScope(RolScope rolScope) {
        TipoPermiso tipoPermiso = this.obtenerTipoPermiso(rolScope.tipoPermisoNombre());

        Establecimiento establecimiento = null;
        if (rolScope.establecimientoId() != null) {
            establecimiento = this.establecimientoRepository
                    .findByIdAndFechaHoraBajaIsNull(rolScope.establecimientoId())
                    .orElseThrow(() -> new AppException(RolError.MALA_REQUEST, "No se encuentra establecimiento con el id " + rolScope.establecimientoId(), null));
        }

        return new RolScopeSolved(tipoPermiso, establecimiento);
    }

    private TipoPermiso obtenerTipoPermiso(TipoPermisoNombre tipoPermisoNombre) {
        return this.tipoPermisoRepository
                .findByNombre(tipoPermisoNombre)
                .orElseThrow(() -> new IllegalStateException("No se encuentra el tipoPermiso ingresado"));
    }
}
