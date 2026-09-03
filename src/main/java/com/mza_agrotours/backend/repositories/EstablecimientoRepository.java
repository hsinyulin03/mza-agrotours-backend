package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.dtos.establecimiento.DTOFiltroCultivoEstablecimiento;
import com.mza_agrotours.backend.dtos.reservas.EstablecimientoPorActividad;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT a.id as actividadId, e as establecimiento FROM Establecimiento e JOIN e.actividades a WHERE a.id IN :actividadIds")
    List<EstablecimientoPorActividad> findEstablecimientosByActividadIds(@Param("actividadIds") List<UUID> actividadIds);

    @Query("""
    SELECT DISTINCT e
        FROM Establecimiento e
        WHERE e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO
        AND e.fechaHoraBaja IS NULL
""")
    List<Establecimiento> obtenerEstablecimientosActivos();

    Optional<Establecimiento> findByIdAndFechaHoraBajaIsNull(UUID id);
    boolean existsByCuitAndFechaHoraBajaIsNull(String cuit);
    boolean existsByTiposCultivosIdAndFechaHoraBajaIsNull(UUID tipoCultivoId);

    @Query("SELECT COUNT(e) > 0 FROM Establecimiento e " +
            "WHERE e.id = :establecimientoId " +
            "AND e.fechaHoraBaja IS NULL " +
            "AND e.titular.usuario.email = :email " +
            "AND e.titular.fechaHoraBaja IS NULL")
    boolean esTitularVigente(@Param("email") String email,
                             @Param("establecimientoId") UUID establecimientoId);
    @Query(
            value = """
        SELECT DISTINCT e FROM Establecimiento e
        JOIN FETCH e.departamento d
        JOIN e.actividades a
        JOIN a.cultivos c
        WHERE e.fechaHoraBaja IS NULL
        AND e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO
        AND a.fechaHoraBaja IS NULL
        AND a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO
        AND (:departamentoId IS NULL OR d.id = :departamentoId)
        AND (:cultivosIds IS NULL OR c.id IN :cultivosIds)
        AND c.fechaHoraBaja IS NULL
    """,
            countQuery = """
        SELECT COUNT(DISTINCT e) FROM Establecimiento e
        JOIN e.actividades a
        JOIN a.cultivos c
        WHERE e.fechaHoraBaja IS NULL
        AND e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO
        AND a.fechaHoraBaja IS NULL
        AND a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO
        AND (:departamentoId IS NULL OR e.departamento.id = :departamentoId)
        AND (:cultivosIds IS NULL OR c.id IN :cultivosIds)
        AND c.fechaHoraBaja IS NULL
    """
    )
    Page<Establecimiento> obtenerEstablecimientosActivos(
            @Param("cultivosIds") List<UUID> cultivosIds,
            @Param("departamentoId") UUID departamentoId,
            Pageable pageable
    );
    @Query("""
    SELECT new com.mza_agrotours.backend.dtos.establecimiento.DTOFiltroCultivoEstablecimiento(c.id, c.nombre, COUNT(DISTINCT e))
    FROM Establecimiento e
    JOIN e.actividades a
    JOIN a.cultivos c
    WHERE e.fechaHoraBaja IS NULL
    AND e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO
    AND a.fechaHoraBaja IS NULL
    AND a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO
    AND c.fechaHoraBaja IS NULL
    GROUP BY c.id, c.nombre
    ORDER BY c.nombre
    """)
    List<DTOFiltroCultivoEstablecimiento> obtenerFiltroCultivos();

}