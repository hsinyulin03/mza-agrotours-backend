package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.PermisoNombre;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermisoRepository extends BaseEntityRepository<Permiso, UUID> {
    Optional<Permiso> findByNombre(PermisoNombre nombre);

    List<Permiso> findByTipoPermiso(TipoPermiso tipoPermiso);
}