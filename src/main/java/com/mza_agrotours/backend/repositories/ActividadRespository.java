package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.actividad.Actividad;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadRespository extends BaseEntityRepository<Actividad, Long> {
    boolean existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);
}
