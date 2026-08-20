package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.AccesoDTO;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccesoMapper {
    /**
     * Mapea un rol a un AccesoDTO.
     * 1. Mapea los permisos del rol a una lista de Strings.
     * 2. Mapea el id del rol y el nombre del rol.
     * 3. Mapea el tipo de permiso del rol.
     * Ignora establecimientoNombre y establecimientoId.
     * @param rol
     * @return AccesoDTO sin los campos establecimientoNombre y establecimientoId mapeados.
     */
    @Mapping(target = "rolId", source = "rol.id")
    @Mapping(target = "rolNombre", source = "rol.nombre")
    @Mapping(target = "tipoPermiso", source = "rol.tipoPermiso.nombre")
    @Mapping(target = "establecimientoNombre", ignore = true)
    @Mapping(target = "establecimientoId", ignore = true)
    AccesoDTO  rolToAccesoDTO(Rol rol);

    default String permisoToString(Permiso permiso) {
        return permiso.getNombre().name();
    }
}
