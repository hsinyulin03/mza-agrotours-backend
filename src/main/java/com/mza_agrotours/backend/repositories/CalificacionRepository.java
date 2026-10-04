package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Calificacion;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CalificacionRepository extends BaseEntityRepository<Calificacion, UUID> {
}
