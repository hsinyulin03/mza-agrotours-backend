package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.AdministradorSistemas;

import java.util.Optional;
import java.util.UUID;

public interface AdministradorSistemasRepository extends BaseEntityRepository<AdministradorSistemas, UUID> {
    Optional<AdministradorSistemas> findByUsuarioId(UUID usuarioId);
}
