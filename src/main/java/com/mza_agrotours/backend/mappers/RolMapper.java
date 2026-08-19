package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.roles_permisos.RolCreateResponse;
import com.mza_agrotours.backend.dtos.roles_permisos.RolGetCatalogoDTO;
import com.mza_agrotours.backend.dtos.roles_permisos.RolGetShortDTO;
import com.mza_agrotours.backend.dtos.roles_permisos.RolUpdateResponse;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RolMapper {
    RolGetShortDTO rolToRolGetShortDTO(Rol rol);

    List<RolGetShortDTO> rolListToRolGetShortDTOList(List<Rol> roles);

    RolCreateResponse rolToRolCreateResponse(Rol rol);

    RolUpdateResponse rolToRolUpdateResponse(Rol rol);

    @Mapping(target = "cantidadUsuarios", ignore = true)
    RolGetCatalogoDTO rolToRolGetCatalogoDTO(Rol rol);

    default String permisoToString(Permiso permiso) {
        return permiso.getCodigo().name();
    }
}
