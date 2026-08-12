package com.mza_agrotours.backend.repositories.receta;

import com.mza_agrotours.backend.entities.receta.Duracion;
import com.mza_agrotours.backend.enums.DuracionNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DuracionRepository extends BaseEntityRepository <Duracion, UUID>{
    @Query("""
        SELECT d FROM Duracion d
        WHERE :minutos >= d.minDesde
        AND (d.minHasta IS NULL OR :minutos <= d.minHasta)
        """)
    Optional<Duracion> findByRangoDeMinutos(@Param("minutos") Integer minutos);
    Optional<Duracion> findByNombre(DuracionNombre nombre);
}
