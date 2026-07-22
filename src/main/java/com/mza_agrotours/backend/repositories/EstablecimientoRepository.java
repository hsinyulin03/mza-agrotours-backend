package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstablecimientoRepository extends BaseEntityRepository<Establecimiento, UUID> {

    boolean existsByCuit(String cuit);

    @Query("select e from Establecimiento e join e.actividades a where a.id = :actId")
    Optional<Establecimiento> findEstablecimientoByActividadId(@Param("actId") UUID actId);

    @Query("""
    SELECT DISTINCT e
    FROM Establecimiento e
    JOIN FETCH e.tiposCultivos tc
    JOIN e.estados ee
    WHERE e.fechaHoraBaja IS NULL
      AND ee.fechaFin IS NULL
      AND ee.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO
      AND ee.estadoEstablecimiento.fechaBaja IS NULL
""")
    List<Establecimiento> obtenerEstablecimientosActivos();
    Optional<Establecimiento> findByIdAndFechaHoraBajaIsNull(UUID id);
}