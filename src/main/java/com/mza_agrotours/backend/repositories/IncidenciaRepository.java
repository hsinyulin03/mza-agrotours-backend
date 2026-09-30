package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.dtos.incidencia.DTOIncidenciaGestionListado;
import com.mza_agrotours.backend.entities.incidencia.Incidencia;
import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenciaRepository extends BaseEntityRepository<Incidencia, UUID> {

    @Query(value = """
            SELECT NEW com.mza_agrotours.backend.dtos.incidencia.DTOIncidenciaGestionListado(
                i.id,
                i.titulo,
                u.nombre,
                i.descripcion,
                i.fechaHoraInicio,
                i.fechaHoraFin,
                e.nombre
            )
            FROM Incidencia i
            JOIN i.usuario u
            JOIN i.estadoActual ea
            JOIN ea.estado e
            WHERE (:estado IS NULL OR e.nombre = :estado)
            AND (
                :busqueda= '' OR
                LOWER(i.titulo) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(i.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(u.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))
            )
            """,
            countQuery = """
            SELECT COUNT(i)
            FROM Incidencia i
            JOIN i.usuario u
            JOIN i.estadoActual ea
            JOIN ea.estado e
            WHERE (:estado IS NULL OR e.nombre = :estado)
            AND (
                :busqueda= '' OR
                LOWER(i.titulo) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(i.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR
                LOWER(u.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))
            )
            """)
    Page<DTOIncidenciaGestionListado> obtenerIncidenciasGestion(
            @Param("busqueda") String busqueda,
            @Param("estado") EstadoIncidenciaNombre estado,
            Pageable pageable);

    @Query("""
        SELECT i.estadoActual.estado.nombre, COUNT(i) FROM Incidencia i
        GROUP BY i.estadoActual.estado.nombre
        """)
    List<Object[]> contarPorEstado();

    @Query("SELECT i FROM Incidencia i WHERE i.usuario.email = :email ORDER BY i.fechaHoraInicio ASC")
    List<Incidencia> findByUsuarioEmailOrderByFechaHoraInicioAsc(@Param("email") String email);

}
