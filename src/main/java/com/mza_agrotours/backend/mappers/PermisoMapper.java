package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.roles_permisos.GrupoPermisoDTO;
import com.mza_agrotours.backend.entities.roles_permisos.GrupoPermiso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermisoMapper {
    List<GrupoPermisoDTO> grupoPermisoListToGrupoPermisoDTOList(List<GrupoPermiso> grupoPermisos);

    @Mapping(target = "permisos", ignore = true)
    GrupoPermisoDTO grupoPermisoToGrupoPermisoDTO(GrupoPermisoDTO grupoPermiso);
}
