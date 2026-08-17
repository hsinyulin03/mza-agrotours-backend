package com.mza_agrotours.backend.services.roles_permisos;

import com.mza_agrotours.backend.dtos.roles_permisos.*;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.RolError;
import com.mza_agrotours.backend.mappers.RolMapper;
import com.mza_agrotours.backend.repositories.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RolService {
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;
    private final RolMapper rolMapper;
    private final TipoPermisoRepository tipoPermisoRepository;
    private final PermisoRepository permisoRepository;
    private final EstablecimientoRepository establecimientoRepository;
    // TODO: ProductorRepository soon

    public RolService(RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      AdministradorSistemasRepository administradorSistemasRepository,
                      RolMapper rolMapper,
                      TipoPermisoRepository tipoPermisoRepository,
                      PermisoRepository permisoRepository,
                      EstablecimientoRepository establecimientoRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.rolMapper = rolMapper;
        this.tipoPermisoRepository = tipoPermisoRepository;
        this.permisoRepository = permisoRepository;
        this.establecimientoRepository = establecimientoRepository;
    }

    public List<PermisoCodigo> obtenerPermisoCodigosAdminPorEmail(String email) {
        return this.administradorSistemasRepository.findPermisoCodigosByEmailActivo(email);
    }

    public List<RolGetCatalogoDTO> obtenerRolesAdminCatalogo() {
        return this.obtenerRolesCatalogoByTipoPermisoNombre(TipoPermisoNombre.ADMIN);
    }

    public RolCreateResponse crearRolAdmin(RolCreateRequest rolCreateRequest) {
        Rol rolCreado = crearRol(rolCreateRequest, RolScope.admin());
        return this.rolMapper.rolToRolCreateResponse(rolCreado);
    }

    public RolUpdateResponse modificarRolAdmin(String rolId, RolUpdateRequest rolUpdateRequest) {
        Rol rolModificado = modificarRol(rolId, rolUpdateRequest, RolScope.admin());
        return this.rolMapper.rolToRolUpdateResponse(rolModificado);
    }

    public boolean bajaRolAdmin(String rolId) {
        return bajaRol(rolId, RolScope.admin());
    }

    private Rol crearRol(RolCreateRequest rolCreateRequest, RolScope rolScope) {
        RolScopeSolved rolScopeSolved = resolverRolScope(rolScope);

        TipoPermisoNombre tipoPermisoNombre = rolScope.tipoPermisoNombre();

        if (this.rolRepository.existsByNombreScoped(
                rolCreateRequest.getNombre(),
                tipoPermisoNombre,
                rolScope.establecimientoId())) {
            throw new AppException(RolError.ROL_ALREADY_EXISTS);
        }

        List<Permiso> permisos = this.resolvePermisos(rolCreateRequest, rolScopeSolved.tipoPermiso());

        Rol nuevoRol = this.rolMapper.rolScopeSolvedAndCreateRequestAndPermisosToRol(rolCreateRequest, rolScopeSolved, permisos);

        return this.rolRepository.save(nuevoRol);
    }

    private Rol modificarRol(String rolId, RolUpdateRequest rolUpdateRequest, RolScope rolScope) {
        TipoPermisoNombre tipoPermisoNombre = rolScope.tipoPermisoNombre();

        RolScopeSolved rolScopeSolved = resolverRolScope(rolScope);
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
                        tipoPermisoNombre,
                        rolScope.establecimientoId())
                .orElseThrow(() -> new AppException(RolError.NOT_FOUND));

        List<Permiso> permisos = this.resolvePermisos(rolUpdateRequest, rolScopeSolved.tipoPermiso());

        rol.setNombre(rolUpdateRequest.getNombre());
        rol.setDescripcion(rolUpdateRequest.getDescripcion());
        rol.setPermisos(permisos);
        return this.rolRepository.save(rol);
    }

    private boolean bajaRol(String rolId, RolScope rolScope) {
        TipoPermisoNombre tipoPermisoNombre = rolScope.tipoPermisoNombre();

        Rol rol = this.rolRepository
                .findVigenteMutableByIdScoped(UUID.fromString(rolId), tipoPermisoNombre, rolScope.establecimientoId())
                .orElseThrow(() -> new AppException(RolError.NOT_FOUND));


        Integer cantidadUsers = obtenerCantidadUsuariosPorRol(rol);
        if (cantidadUsers > 0) {
            throw new AppException(RolError.BAJA_ROL_CON_USUARIOS);
        }

        rol.setFechaHoraBaja(LocalDateTime.now());
        this.rolRepository.save(rol);
        return true;
    }

    private List<RolGetCatalogoDTO> obtenerRolesCatalogoByTipoPermisoNombre(TipoPermisoNombre tipoPermisoNombre) {
        List<Rol> rolesByTipoPermiso = this.rolRepository
                .findByTipoPermiso_NombreAndFechaHoraBajaIsNull(tipoPermisoNombre);

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
            // TODO: ProductorRepository soon
            case PRODUCTOR -> 0;
            default -> throw new IllegalStateException("Unexpected value: " + tipoPermisoNombre);
        };
    }

    private List<Permiso> resolvePermisos(RolCreateRequest rolCreateRequest, TipoPermiso tipoPermiso) {
        List<Permiso> permisos = this.permisoRepository
                .findByTipoPermisoAndCodigoIn(tipoPermiso, rolCreateRequest.getPermisos());

        if (permisos.size() != rolCreateRequest.getPermisos().size()) {
            throw new AppException(RolError.PERMISO_INVALIDO);
        }

        return permisos;
    }

    private RolScopeSolved resolverRolScope(RolScope rolScope) {
        TipoPermisoNombre tipoPermisoNombre = rolScope.tipoPermisoNombre();

        TipoPermiso tipoPermiso = this.tipoPermisoRepository
                .findByNombre(tipoPermisoNombre)
                .orElseThrow(() -> new IllegalStateException("No se encuentra el tipoPermiso ingresado"));

        Establecimiento establecimiento = null;
        if (rolScope.establecimientoId() != null) {
            establecimiento = this.establecimientoRepository
                    .findByIdAndFechaHoraBajaIsNull(rolScope.establecimientoId())
                    .orElseThrow(() -> new AppException(RolError.MALA_REQUEST, "No se encuentra establecimiento con el id " + rolScope.establecimientoId(), null));
        }

        return new RolScopeSolved(tipoPermiso, establecimiento);
    }
}
