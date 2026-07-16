package com.mza_agrotours.backend.repositories.actividad;


import com.mza_agrotours.backend.entities.actividad.EstadoActividad;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadoActividadRepository extends BaseEntityRepository<EstadoActividad, UUID> {
    Optional<EstadoActividad> findByNombre(EstadoActividadNombre nombre);
    boolean existsByNombre(EstadoActividadNombre nombre);
}
