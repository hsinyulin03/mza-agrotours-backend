package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.roles_permisos.RolGetShortDTO;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RolMapper {
    RolGetShortDTO rolToRolGetShortDTO(Rol rol);

    List<RolGetShortDTO> rolListToRolGetShortDTOList(List<Rol> roles);
}
