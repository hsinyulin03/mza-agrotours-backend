package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.dtos.establecimiento.DTOFiltroCultivoEstablecimiento;
import com.mza_agrotours.backend.dtos.establecimiento.DTOFiltroDepartamentoEstablecimiento;
import com.mza_agrotours.backend.dtos.PanelSistema.EstablecimientoAdminDto;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
        WHERE e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO
        AND e.fechaHoraBaja IS NULL
""")
    List<Establecimiento> obtenerEstablecimientosActivos();

    @Query("SELECT e FROM Establecimiento e " +
            "WHERE e.id = :establecimientoId " +
            "AND e.fechaHoraBaja IS NULL " +
            "AND e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO")
    Optional<Establecimiento> obtenerEstablecimientoActivoById(@Param("establecimientoId") UUID establecimientoId);

    List<Establecimiento> findByFechaHoraBajaIsNull();

    Optional<Establecimiento> findByIdAndFechaHoraBajaIsNull(UUID id);
    boolean existsByCuitAndFechaHoraBajaIsNull(String cuit);
    boolean existsByTiposCultivosIdAndFechaHoraBajaIsNull(UUID tipoCultivoId);

    @Query("SELECT COUNT(e) > 0 FROM Establecimiento e " +
            "WHERE e.nombre = :nombre " +
            "AND e.id <> :id " +
            "AND e.fechaHoraBaja IS NULL")
    boolean existsEstablecimientoVigenteByNombreOtherThanId(@Param("nombre") String nombre, @Param("id") UUID id);

    @Query("SELECT COUNT(e) > 0 FROM Establecimiento e " +
            "WHERE e.id = :establecimientoId " +
            "AND e.fechaHoraBaja IS NULL " +
            "AND e.titular.usuario.email = :email " +
            "AND e.titular.fechaHoraBaja IS NULL")
    boolean esTitularVigente(@Param("email") String email,
                             @Param("establecimientoId") UUID establecimientoId);

    @Query("SELECT COUNT(e) > 0 FROM Establecimiento e " +
            "WHERE e.id = :establecimientoId " +
            "AND e.fechaHoraBaja IS NULL " +
            "AND e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.SUSPENDIDO")
    boolean establecimientoSuspendido(@Param("establecimientoId") UUID establecimientoId);

    @Query(
            value = """
        SELECT e FROM Establecimiento e
        JOIN FETCH e.departamento
        WHERE e.fechaHoraBaja IS NULL
        AND e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO
        AND (CAST(:busqueda AS string) IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%')))
        AND (:departamentoId IS NULL OR e.departamento.id = :departamentoId)
        AND (:cultivosIds IS NULL OR EXISTS (
            SELECT 1 FROM e.actividades a
            JOIN a.cultivos c
            WHERE a.fechaHoraBaja IS NULL
            AND a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO
            AND c.fechaHoraBaja IS NULL
            AND c.id IN :cultivosIds
        ))
    """,
            countQuery = """
        SELECT COUNT(e) FROM Establecimiento e
        WHERE e.fechaHoraBaja IS NULL
        AND e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO
        AND (CAST(:busqueda AS string) IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%')))
        AND (:departamentoId IS NULL OR e.departamento.id = :departamentoId)
        AND (:cultivosIds IS NULL OR EXISTS (
            SELECT 1 FROM e.actividades a
            JOIN a.cultivos c
            WHERE a.fechaHoraBaja IS NULL
            AND a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO
            AND c.fechaHoraBaja IS NULL
            AND c.id IN :cultivosIds
        ))
    """
    )
    Page<Establecimiento> obtenerEstablecimientosActivos(
            @Param("busqueda") String busqueda,
            @Param("cultivosIds") List<UUID> cultivosIds,
            @Param("departamentoId") UUID departamentoId,
            Pageable pageable
    );
    // Landing: establecimientos que admiten reservas (ver Establecimiento.admiteReservas)
    @Query("""
        SELECT e FROM Establecimiento e
        JOIN FETCH e.departamento
        WHERE e.fechaHoraBaja IS NULL
        AND e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO
        AND EXISTS (
            SELECT 1 FROM CuentaMercadoPago c
            WHERE c.establecimiento = e
            AND c.fechaHoraBaja IS NULL
            AND c.fechaHoraExpiracionToken > :ahora
        )
        ORDER BY e.fechaHoraAlta DESC, e.id
    """)
    List<Establecimiento> obtenerEstablecimientosDestacados(@Param("ahora") LocalDateTime ahora, Pageable pageable);

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
    @Query("SELECT COUNT(e) FROM Establecimiento e WHERE e.estadoActual.estadoEstablecimiento.nombre = :nombre AND e.fechaHoraBaja IS NULL")
    long countByEstadoNombre(@Param("nombre") EstadoEstablecimientoNombre nombre);

    @Query("SELECT new com.mza_agrotours.backend.dtos.PanelSistema.EstablecimientoAdminDto(e.id, e.nombre) FROM Establecimiento e WHERE e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.SUSPENDIDO AND e.fechaHoraBaja IS NULL")
    List<EstablecimientoAdminDto> listarSuspendidos();

    @Query("""
    SELECT new com.mza_agrotours.backend.dtos.establecimiento.DTOFiltroDepartamentoEstablecimiento(d.id, d.nombre, COUNT(DISTINCT e))
    FROM Establecimiento e
    JOIN e.departamento d
    WHERE e.fechaHoraBaja IS NULL
    AND e.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO
    GROUP BY d.id, d.nombre
    ORDER BY d.nombre ASC
    """)
    List<DTOFiltroDepartamentoEstablecimiento> obtenerFiltroDepartamentos();

}