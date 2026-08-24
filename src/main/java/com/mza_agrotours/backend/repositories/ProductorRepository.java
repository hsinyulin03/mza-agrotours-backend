package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductorRepository extends BaseEntityRepository<Productor, UUID> {

    Integer countByRolAndFechaHoraBajaIsNull(Rol rol);

    @Query("SELECT COUNT(pr) > 0 FROM Productor pr " +
            "JOIN pr.rol r JOIN r.permisos p " +
            "WHERE pr.usuario.email = :email " +
            "AND pr.establecimiento.id = :establecimientoId " +
            "AND pr.establecimiento.fechaHoraBaja IS NULL " +
            "AND pr.fechaHoraBaja IS NULL " +
            "AND r.fechaHoraBaja IS NULL " +
            "AND p.codigo = :permiso " +
            "AND pr.estadoActual.nombre = 'ACTIVO'")
    boolean tienePermisoEnEstablecimiento(
            @Param("email") String email,
            @Param("establecimientoId") UUID establecimientoId,
            @Param("permiso") PermisoCodigo permiso);
    List<Productor> findByUsuarioAndFechaHoraBajaIsNull(Usuario usuario);

    Optional<Productor> findByIdAndFechaHoraBajaIsNull(UUID id);

    /**
     * Busca el productor dentro del establecimiento indicado. El scope no es cosmetico:
     * evita que quien tiene permisos sobre un establecimiento opere productores de otro
     * conociendo su id.
     */
    Optional<Productor> findByIdAndEstablecimiento_IdAndFechaHoraBajaIsNull(UUID id, UUID establecimientoId);

    boolean existsByUsuarioAndEstablecimiento_IdAndFechaHoraBajaIsNull(Usuario usuario, UUID establecimientoId);

    @Query("SELECT DISTINCT p FROM Productor p " +
            "LEFT JOIN FETCH p.estados " +
            "JOIN FETCH p.usuario " +
            "JOIN FETCH p.rol " +
            "JOIN FETCH p.estadoActual " +
            "WHERE p.establecimiento.id = :establecimientoId " +
            "AND p.fechaHoraBaja IS NULL")
    List<Productor> findVigentesByEstablecimiento(@Param("establecimientoId") UUID establecimientoId);

    /**
     * Ids de productores cuyo tramo de estado vigente tiene un vencimiento planificado ya cumplido.
     * No hace falta filtrar por nombre de estado ni ordenar: "tramo abierto + fecha prevista
     * pasada" ya describe exactamente el conjunto de suspensiones a levantar.
     * Devuelve ids y no entidades para que cada reactivacion recargue el productor dentro de
     * su propia transaccion, en lugar de arrastrar entidades detached entre transacciones.
     */
    @Query("SELECT p.id FROM Productor p " +
            "WHERE p.fechaHoraBaja IS NULL " +
            "AND EXISTS (SELECT e FROM p.estados e " +
            "            WHERE e.fechaHoraFin IS NULL " +
            "            AND e.fechaHoraFinPrevista IS NOT NULL " +
            "            AND e.fechaHoraFinPrevista <= :ahora)")
    List<UUID> findIdsConSuspensionVencida(@Param("ahora") LocalDateTime ahora);
}
