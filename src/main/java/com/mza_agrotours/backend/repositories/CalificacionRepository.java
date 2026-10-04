package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.dtos.calificacion.DistribucionPuntajeDTO;
import com.mza_agrotours.backend.dtos.calificacion.ReseniaCardDTO;
import com.mza_agrotours.backend.entities.Calificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CalificacionRepository extends BaseEntityRepository<Calificacion, UUID> {

    //US-ACT-02: Agrupa cantidad de reseñas por puntaje
    @Query("SELECT new com.mza_agrotours.backend.dtos.calificacion.DistribucionPuntajeDTO(c.puntaje, COUNT(c)) " +
            "FROM Actividad a " +
            "JOIN a.calificaciones c " +
            "WHERE a.id = :actividadId " +
            "GROUP BY c.puntaje")
    List<DistribucionPuntajeDTO> contarPorPuntaje(@Param("actividadId") UUID actividadId);

    //US-ACT-02: Reseñas de una actividad, paginadas y con filtro opcional por puntaje.
    @Query(value = "SELECT new com.mza_agrotours.backend.dtos.calificacion.ReseniaCardDTO(" +
            "  u.nombre, c.fechaHoraCalificacion, c.puntaje, c.resenia) " +
            "FROM Calificacion c " +
            "JOIN Reserva r ON r.calificacion = c " +
            "JOIN r.visitante v " +
            "JOIN v.usuario u " +
            "WHERE r.actividad.id = :actividadId " +
            "AND (:puntaje IS NULL OR c.puntaje = :puntaje)",
            countQuery = "SELECT COUNT(c) " +
                    "FROM Calificacion c " +
                    "JOIN Reserva r ON r.calificacion = c " +
                    "WHERE r.actividad.id = :actividadId " +
                    "AND (:puntaje IS NULL OR c.puntaje = :puntaje)")
    Page<ReseniaCardDTO> findReseniasByActividad(@Param("actividadId") UUID actividadId,
                                                 @Param("puntaje") Integer puntaje,
                                                 Pageable pageable);
}
