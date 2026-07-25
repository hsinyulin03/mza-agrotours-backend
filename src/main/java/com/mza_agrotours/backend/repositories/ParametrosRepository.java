package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Parametros;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParametrosRepository extends BaseEntityRepository<Parametros, UUID> {
    Optional<Parametros> findFirstByOrderByIdAsc();
}