package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.acceso.AccesoDTO;
import com.mza_agrotours.backend.dtos.acceso.AccesoEstablecimientoDTO;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccesoMapper {
    /**
     * Mapea un rol a un AccesoDTO, con sus permisos y detalles del establecimiento si aplicasen.
     * Tiene limitaciones para obtener información del Productor, si se precisa, se deberá obtener
     * por repository fuera del mapper
     * @param rol
     * @return AccesoDTO sin los campos establecimientoNombre y establecimientoId mapeados.
     */
    default AccesoDTO rolToAccesoDTO(Rol rol) {
        AccesoDTO accesoDTO = new AccesoDTO();
        accesoDTO.setRolId(rol.getId().toString());
        accesoDTO.setRolNombre(rol.getNombre());
        accesoDTO.setTipoPermiso(rol.getTipoPermiso().getNombre());
        accesoDTO.setPermisos(rol.getPermisos().stream().map(this::permisoToString).toList());


        if (rol.getEstablecimiento() == null) {
            return accesoDTO;
        }

        AccesoEstablecimientoDTO accesoEstablecimientoDTO = new AccesoEstablecimientoDTO();
        accesoEstablecimientoDTO.setId(rol.getEstablecimiento().getId().toString());
        accesoEstablecimientoDTO.setNombre(rol.getEstablecimiento().getNombre());
        accesoEstablecimientoDTO.setEstado(rol.getEstablecimiento().getEstadoActual().getEstadoEstablecimiento().getNombre().name());

        accesoDTO.setEstablecimiento(accesoEstablecimientoDTO);
        return accesoDTO;
    }

    default String permisoToString(Permiso permiso) {
        return permiso.getCodigo().name();
    }
}
