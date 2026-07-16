package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EstablecimientoRepository extends BaseEntityRepository<Establecimiento, UUID>{
    @Query("select e from Establecimiento e join e.actividades a where a.id = :actId")
    Optional<Establecimiento> findEstablecimientoByActividadId(@Param("actId") UUID actId);
}
