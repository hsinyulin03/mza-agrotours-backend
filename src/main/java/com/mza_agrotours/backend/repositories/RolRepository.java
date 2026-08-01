package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.roles_permisos.Rol;

import java.util.UUID;

public interface RolRepository extends BaseEntityRepository<Rol, UUID> {
    boolean existsByNombre(String nombre);
}

