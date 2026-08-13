package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.roles_permisos.*;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.PermisoNombre;
import com.mza_agrotours.backend.enums.RolProtegido;
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
    // TODO: ProductorRepository soon

    public RolService(RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      AdministradorSistemasRepository administradorSistemasRepository,
                      RolMapper rolMapper,
                      TipoPermisoRepository tipoPermisoRepository,
                      PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.rolMapper = rolMapper;
        this.tipoPermisoRepository = tipoPermisoRepository;
        this.permisoRepository = permisoRepository;
    }

    public List<PermisoNombre> obtenerPermisosAdminPorEmail(String email) {
        return this.administradorSistemasRepository.findPermisoNombresByEmailActivo(email);
    }

    public List<RolGetCatalogoDTO> obtenerRolesAdminCatalogo() {
        return this.obtenerRolesCatalogoByTipoPermisoNombre(TipoPermisoNombre.ADMIN);
    }

    public RolCreateResponse crearRolAdmin(RolCreateRequest rolCreateRequest) {
        Rol rolCreado = crearRol(rolCreateRequest, TipoPermisoNombre.ADMIN);
        return this.rolMapper.rolToRolCreateResponse(rolCreado);
    }

    public RolUpdateResponse modificarRolAdmin(String rolId, RolUpdateRequest rolUpdateRequest) {
        Rol rolModificado = modificarRol(rolId, rolUpdateRequest, TipoPermisoNombre.ADMIN);
        return this.rolMapper.rolToRolUpdateResponse(rolModificado);
    }

    public boolean bajaRolAdmin(String rolId) {
        return bajaRol(rolId, TipoPermisoNombre.ADMIN);
    }

    private Rol crearRol(RolCreateRequest rolCreateRequest, TipoPermisoNombre tipoPermisoNombre) {
        TipoPermiso tipoPermiso = this.tipoPermisoRepository
                .findByNombre(tipoPermisoNombre)
                .orElseThrow(() -> new IllegalStateException("Invalidaso"));

        List<Permiso> permisos = this.permisoRepository
                .findByTipoPermisoAndNombreIn(tipoPermiso, rolCreateRequest.getPermisos());

        if (permisos.size() != rolCreateRequest.getPermisos().size()) {
            throw new AppException(RolError.PERMISO_INVALIDO);
        }
        // TODO: fetchear roles existentess


        Rol nuevoRol = new Rol();
        nuevoRol.setNombre(rolCreateRequest.getNombre());
        nuevoRol.setDescripcion(rolCreateRequest.getDescripcion());
        nuevoRol.setEsProtegido(false);
        nuevoRol.setPermisos(permisos);
        nuevoRol.setTipoPermiso(tipoPermiso);

        return this.rolRepository.save(nuevoRol);
    }

    private Rol modificarRol(String rolId, RolUpdateRequest rolUpdateRequest, TipoPermisoNombre tipoPermisoNombre) {
        TipoPermiso tipoPermiso = this.tipoPermisoRepository
                .findByNombre(tipoPermisoNombre)
                .orElseThrow(() -> new IllegalStateException("Invalidaso"));
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
                .findVigenteByIdAndTipoPermisoNombre(UUID.fromString(rolId), tipoPermisoNombre)
                .orElseThrow(() -> new AppException(RolError.NOT_FOUND));

        List<Permiso> permisos = this.permisoRepository
                .findByTipoPermisoAndNombreIn(tipoPermiso, rolUpdateRequest.getPermisos());

        if (permisos.size() != rolUpdateRequest.getPermisos().size()) {
            throw new AppException(RolError.PERMISO_INVALIDO);
        }

        rol.setNombre(rolUpdateRequest.getNombre());
        rol.setDescripcion(rolUpdateRequest.getDescripcion());
        rol.setPermisos(permisos);
        return this.rolRepository.save(rol);
    }

    private boolean bajaRol(String rolId, TipoPermisoNombre tipoPermisoNombre) {
        String rolExcluido = getRolExcluidoByTipoPermisoNombre(tipoPermisoNombre);

        Rol rol = this.rolRepository
                .findVigenteByIdScoped(UUID.fromString(rolId), tipoPermisoNombre, rolExcluido)
                .orElseThrow(() -> new AppException(RolError.NOT_FOUND));

        rol.setFechaHoraBaja(LocalDateTime.now());
        this.rolRepository.save(rol);
        return true;
    }

    private String getRolExcluidoByTipoPermisoNombre(TipoPermisoNombre tipoPermisoNombre) {
        return switch (tipoPermisoNombre) {
            case ADMIN -> RolProtegido.ADMIN_LIDER.getNombre();
            case PRODUCTOR -> RolProtegido.PRODUCTOR_LIDER.getNombre();
            default -> throw new IllegalStateException("Unexpected value: " + tipoPermisoNombre);
        };
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
}
