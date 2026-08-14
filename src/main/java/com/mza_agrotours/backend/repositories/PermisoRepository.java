package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermisoRepository extends BaseEntityRepository<Permiso, UUID> {
    Optional<Permiso> findByCodigo(PermisoCodigo codigo);

    List<Permiso> findByTipoPermiso(TipoPermiso tipoPermiso);

    @Query("select p from Permiso p where p.codigo in :codigos and p.tipoPermiso = :tipoPermiso")
    List<Permiso> findByTipoPermisoAndCodigoIn(@Param("tipoPermiso") TipoPermiso tipoPermiso,
                                               @Param("codigos") List<String> codigos);
}