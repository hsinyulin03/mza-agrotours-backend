package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.roles_permisos.*;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.services.roles_permisos.RolScopeSolved;
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

    @Mapping(target = "permisos", source = "permisos")
    @Mapping(target = "establecimiento", source = "rolScopeSolved.establecimiento")
    @Mapping(target = "tipoPermiso", source = "rolScopeSolved.tipoPermiso")
    @Mapping(target = "esProtegido", defaultValue = "false")
    Rol rolScopeSolvedAndCreateRequestAndPermisosToRol(RolCreateRequest rolCreateRequest, RolScopeSolved rolScopeSolved, List<Permiso> permisos);

    default String permisoToString(Permiso permiso) {
        return permiso.getCodigo().name();
    }
}
