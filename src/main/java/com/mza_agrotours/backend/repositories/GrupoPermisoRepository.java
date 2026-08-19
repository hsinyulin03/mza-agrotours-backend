package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.roles_permisos.GrupoPermiso;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GrupoPermisoRepository extends BaseEntityRepository<GrupoPermiso, UUID> {
    List<GrupoPermiso> findAllByTipoPermiso_Nombre(TipoPermisoNombre tipoPermisoNombre);

    Optional<GrupoPermiso> findByNombre(String nombre);
}
