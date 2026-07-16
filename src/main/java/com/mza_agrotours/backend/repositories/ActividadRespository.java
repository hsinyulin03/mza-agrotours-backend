package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.actividad.Actividad;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActividadRespository extends BaseEntityRepository<Actividad, UUID> {
    boolean existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);
}
