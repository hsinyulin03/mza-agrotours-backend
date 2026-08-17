package com.mza_agrotours.backend.services.roles_permisos;

import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;

public record RolScopeSolved(TipoPermiso tipoPermiso, Establecimiento establecimiento) {
    public RolScopeSolved {
        if (tipoPermiso == null) {
            throw new IllegalArgumentException("Tipo de permiso es obligatorio");
        }

        if (tipoPermiso.getNombre().equals(TipoPermisoNombre.PRODUCTOR) && establecimiento == null) {
            throw new IllegalArgumentException("Establecimiento es requerido para rol PRODUCTOR");
        }

        if (tipoPermiso.getNombre().equals(TipoPermisoNombre.ADMIN) && establecimiento != null) {
            throw new IllegalArgumentException("No se puede crear un rol ADMIN con Establecimiento ID");
        }
    }
}
