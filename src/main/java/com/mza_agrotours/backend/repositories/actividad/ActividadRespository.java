package com.mza_agrotours.backend.repositories.actividad;

import com.mza_agrotours.backend.dtos.actividad.DiaActividadReservaDTO;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Repository
public interface ActividadRespository extends BaseEntityRepository<Actividad, UUID> {
    Optional<Actividad>  findByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);
    Optional<Actividad> findByIdAndFechaHoraBajaIsNull(UUID id);

    @Query("SELECT a FROM Actividad a WHERE (:busqueda IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))) " +
            "AND (:estado IS NULL OR a.estado.nombre = :estado)")
    List<Actividad> findByFiltrosDinamicos(
            @Param("busqueda") String busqueda,
            @Param("estado") EstadoActividadNombre estado
    );

    //TODO - Ignoramos temporalmente los filtros de departamento
    @Query("SELECT a FROM Actividad a " +
            "WHERE a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO " +
            "AND a.fechaHoraBaja IS NULL")
    List<Actividad> explorarActividadesPublicadas();

    @Query("SELECT DISTINCT a FROM Actividad a " +
            "LEFT JOIN a.cultivos c " +
            "WHERE a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO " +
            "AND a.fechaHoraBaja IS NULL " +
            "AND (c.id IN :cultivosIds )")
    List<Actividad> explorarActividadesPublicadas(@Param("cultivosIds") List <UUID> cultivosIds);

    @Query("SELECT MAX(ad.fechaHoraInicio) FROM Actividad a JOIN a.actividadesDias ad WHERE a.id = :actividadId")
    Optional<LocalDateTime> findUltimaFechaByActividadId(@Param("actividadId") UUID actividadId);

    @Query("SELECT a FROM Actividad a " +
            "JOIN a.actividadesDias ad " +
            "WHERE ad.id = :uuid")
    Optional<Actividad> getActividadByDiaActividadId(@Param("uuid") UUID uuidDiaActividad);

    @Query("SELECT NEW com.mza_agrotours.backend.dtos.actividad.DiaActividadReservaDTO(" +
            "CAST(ad.id AS string), ad.cuposMax, CAST(COUNT(rd) as int), ad.fechaHoraInicio, ad.fechaHoraFin) " +
            "FROM Actividad a JOIN a.actividadesDias ad " +
            "LEFT JOIN Reserva r ON  r.actividadDia = ad " +
            "AND r.estadoActual.estadoReserva.nombre IN (com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE, com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA) " +
            "LEFT JOIN r.reservaDetalles rd " +
            "WHERE a.id = :uuid " +
            "AND ad.estadoActual.estado.nombre IN (com.mza_agrotours.backend.enums.EstadoActividadDiaNombre.ACTIVA,com.mza_agrotours.backend.enums.EstadoActividadDiaNombre.REPROGRAMADA)" +
            "AND ad.fechaHoraInicio > CURRENT_TIMESTAMP " +
            "GROUP BY ad.id, ad.cuposMax, ad.fechaHoraInicio, ad.fechaHoraFin")
    List<DiaActividadReservaDTO> getDiaActividadReservaDTO(@Param("uuid") UUID uuidActividad);
}
