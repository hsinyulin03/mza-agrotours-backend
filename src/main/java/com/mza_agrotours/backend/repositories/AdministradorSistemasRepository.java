package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdministradorSistemasRepository extends BaseEntityRepository<AdministradorSistemas, UUID> {
    Optional<AdministradorSistemas> findByUsuarioAndFechaHoraBajaIsNull(Usuario usuario);

    boolean existsByUsuarioAndFechaHoraBajaIsNull(Usuario usuario);

    List<AdministradorSistemas> findByFechaHoraBajaIsNull();
}
