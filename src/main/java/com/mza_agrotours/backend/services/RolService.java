package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.roles_permisos.RolCreateRequest;
import com.mza_agrotours.backend.dtos.roles_permisos.RolCreateResponse;
import com.mza_agrotours.backend.dtos.roles_permisos.RolGetCatalogoDTO;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.PermisoNombre;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.RolError;
import com.mza_agrotours.backend.mappers.RolMapper;
import com.mza_agrotours.backend.repositories.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    private Rol crearRol(RolCreateRequest rolCreateRequest, TipoPermisoNombre tipoPermisoNombre) {
        TipoPermiso tipoPermiso = this.tipoPermisoRepository
                .findByNombre(tipoPermisoNombre)
                .orElseThrow(() -> new IllegalStateException("Invalidaso"));

        List<Permiso> permisos = this.permisoRepository
                .findByTipoPermisoAndNombreIn(tipoPermiso, rolCreateRequest.getPermisos());

        if (permisos.size() != rolCreateRequest.getPermisos().size()) {
            throw new AppException(RolError.PERMISO_INVALIDO);
        }

        Rol nuevoRol = new Rol();
        nuevoRol.setNombre(rolCreateRequest.getNombre());
        nuevoRol.setDescripcion(rolCreateRequest.getDescripcion());
        nuevoRol.setEsProtegido(false);
        nuevoRol.setPermisos(permisos);
        nuevoRol.setTipoPermiso(tipoPermiso);

        return this.rolRepository.save(nuevoRol);
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
