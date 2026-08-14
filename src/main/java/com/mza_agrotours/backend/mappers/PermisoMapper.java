package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.roles_permisos.GPPermisoDTO;
import com.mza_agrotours.backend.dtos.roles_permisos.GrupoPermisoDTO;
import com.mza_agrotours.backend.entities.roles_permisos.GrupoPermiso;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermisoMapper {
    List<GrupoPermisoDTO> grupoPermisoListToGrupoPermisoDTOList(List<GrupoPermiso> grupoPermisos);

    GrupoPermisoDTO grupoPermisoToGrupoPermisoDTO(GrupoPermiso grupoPermiso);


    GPPermisoDTO permisoToGPPermisoDTO(Permiso permiso);
}
