package com.mza_agrotours.backend.repositories.actividad;

import com.mza_agrotours.backend.entities.actividad.EstadoActividadDia;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadoActividadDiaRepository extends BaseEntityRepository<EstadoActividadDia, UUID> {
    Optional<EstadoActividadDia> findByNombre(EstadoActividadDiaNombre nombre);
    boolean existsByNombre(EstadoActividadDiaNombre nombre);
}

