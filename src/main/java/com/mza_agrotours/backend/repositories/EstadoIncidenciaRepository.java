package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.incidencia.EstadoIncidencia;
import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadoIncidenciaRepository extends BaseEntityRepository<EstadoIncidencia, UUID> {
    Optional<EstadoIncidencia> findByNombre(EstadoIncidenciaNombre nombre);
    boolean existsByNombre(EstadoIncidenciaNombre nombre);
}
