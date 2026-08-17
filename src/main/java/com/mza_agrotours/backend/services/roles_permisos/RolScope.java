package com.mza_agrotours.backend.services.roles_permisos;

import com.mza_agrotours.backend.enums.TipoPermisoNombre;

import java.util.UUID;

/**
 * Crea un scope para la gestión de roles y permisos.
 * @throws IllegalArgumentException cuando TP es ADMIN y establecimientoId != null.
 * @throws IllegalArgumentException cuando TP es PRODUCTOR y establecimientoId == null.
 * @param tipoPermisoNombre
 * @param establecimientoId
 */
public record RolScope(TipoPermisoNombre tipoPermisoNombre, UUID establecimientoId) {
    public RolScope {
        if (tipoPermisoNombre == null) {
            throw new IllegalArgumentException("Tipo de permiso es obligatorio");
        }
        if (tipoPermisoNombre == TipoPermisoNombre.ADMIN && establecimientoId != null) {
            throw new IllegalArgumentException("No se puede crear un rol ADMIN con Establecimiento ID");
        }
        if (tipoPermisoNombre == TipoPermisoNombre.PRODUCTOR && establecimientoId == null) {
            throw new IllegalArgumentException("Establecimiento ID es requerido para rol PRODUCTOR");
        }
    }

    public static RolScope admin() {
        return new RolScope(TipoPermisoNombre.ADMIN, null);
    }

    public static RolScope productor(UUID establecimientoId) {
        return new RolScope(TipoPermisoNombre.PRODUCTOR, establecimientoId);
    }
}
