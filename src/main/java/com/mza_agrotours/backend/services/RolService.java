package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.roles_permisos.RolGetCatalogoDTO;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.PermisoNombre;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.mappers.RolMapper;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RolService {
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;
    private final RolMapper rolMapper;
    // TODO: ProductorRepository soon

    public RolService(RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      AdministradorSistemasRepository administradorSistemasRepository,
                      RolMapper rolMapper) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.rolMapper = rolMapper;
    }

    public List<PermisoNombre> obtenerPermisosAdminPorEmail(String email) {
        return this.administradorSistemasRepository.findPermisoNombresByEmailActivo(email);
    }

    public List<RolGetCatalogoDTO> obtenerRolesAdminCatalogo() {
        return this.obtenerRolesCatalogoByTipoPermisoNombre(TipoPermisoNombre.ADMIN);
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
