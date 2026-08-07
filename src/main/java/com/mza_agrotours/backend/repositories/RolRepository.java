package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolRepository extends BaseEntityRepository<Rol, UUID> {
    boolean existsByNombre(String nombre);

    Optional<Rol> findByNombre(String nombre);

    Optional<Rol> findByIdAndTipoPermiso_NombreAndFechaHoraBajaIsNullAndNombreIsNotContaining(UUID id, TipoPermisoNombre tipoPermiso_nombre, String nombre);

    List<Rol> findByTipoPermiso_NombreAndFechaHoraBajaIsNullAndNombreIsNotContaining(TipoPermisoNombre tipoPermiso_nombre, String nombre);
}

