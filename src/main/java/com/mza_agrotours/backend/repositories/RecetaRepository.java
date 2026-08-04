package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.receta.Receta;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecetaRepository extends BaseEntityRepository<Receta, UUID> {
    long countByFechaHoraBajaIsNull();
}