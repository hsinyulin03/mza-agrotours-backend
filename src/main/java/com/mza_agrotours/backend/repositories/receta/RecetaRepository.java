package com.mza_agrotours.backend.repositories.receta;

import com.mza_agrotours.backend.dtos.receta.DTOFiltroDificultadReceta;
import com.mza_agrotours.backend.dtos.receta.DTOFiltroDuracionReceta;
import com.mza_agrotours.backend.entities.receta.Receta;
import com.mza_agrotours.backend.enums.Dificultad;
import com.mza_agrotours.backend.enums.DuracionNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecetaRepository extends BaseEntityRepository<Receta, UUID> {
    long countByFechaHoraBajaIsNull();

    boolean existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);

    Optional<Receta> findByIdAndFechaHoraBajaIsNull(UUID id);
    Optional<Receta> findByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);

    List<Receta> findAllByFechaHoraBajaIsNull();


    @Query("""
    SELECT new com.mza_agrotours.backend.dtos.receta.DTOFiltroDificultadReceta(r.dificultad, COUNT(r))
    FROM Receta r
    WHERE r.fechaHoraBaja IS NULL
    GROUP BY r.dificultad
    """)
    List<DTOFiltroDificultadReceta> obtenerFiltroDificultad();

    @Query("""
    SELECT new com.mza_agrotours.backend.dtos.receta.DTOFiltroDuracionReceta(r.duracion.nombre, COUNT(r))
    FROM Receta r
    WHERE r.fechaHoraBaja IS NULL
    GROUP BY r.duracion.nombre
    """)
    List<DTOFiltroDuracionReceta> obtenerFiltroDuracion();

    @Query(value = """
    SELECT r FROM Receta r
    WHERE r.fechaHoraBaja IS NULL
    AND (:cultivoId IS NULL OR r.id IN (
        SELECT r2.id FROM TipoCultivo tc JOIN tc.recetas r2 WHERE tc.id = :cultivoId
    ))
    AND (:dificultad IS NULL OR r.dificultad = :dificultad)
    AND (:duracion IS NULL OR r.duracion.nombre = :duracion)
    """,
    countQuery = """
    SELECT COUNT(r) FROM Receta r
    WHERE r.fechaHoraBaja IS NULL
    AND (:cultivoId IS NULL OR r.id IN (
        SELECT r2.id FROM TipoCultivo tc JOIN tc.recetas r2 WHERE tc.id = :cultivoId
    ))
    AND (:dificultad IS NULL OR r.dificultad = :dificultad)
    AND (:duracion IS NULL OR r.duracion.nombre = :duracion)
    """)
    Page<Receta> consultarCatalogoVisitante(
            @Param("cultivoId") UUID cultivoId,
            @Param("dificultad") Dificultad dificultad,
            @Param("duracion") DuracionNombre duracion,
            Pageable pageable);
}