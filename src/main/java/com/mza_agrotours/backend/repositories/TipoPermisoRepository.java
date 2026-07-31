package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermisoNombre;

import java.util.Optional;
import java.util.UUID;

public interface TipoPermisoRepository extends BaseEntityRepository<TipoPermiso, UUID> {
    boolean existsByNombre(TipoPermisoNombre nombre);
    Optional<TipoPermiso> findByNombre(TipoPermisoNombre nombre);
}
