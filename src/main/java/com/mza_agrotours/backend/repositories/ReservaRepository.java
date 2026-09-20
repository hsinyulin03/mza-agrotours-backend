package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.dtos.actividad.DTOConteoReservasPorActividad;
import com.mza_agrotours.backend.dtos.actividad.DTOReservasBloqueantes;
import com.mza_agrotours.backend.dtos.actividad.DTOCuposPorDia;
import com.mza_agrotours.backend.dtos.actividad.DTOMetricasReservasGlobales;
import com.mza_agrotours.backend.dtos.administrador_sistemas.ConteoPorEstablecimientoDTO;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ReservaRepository extends BaseEntityRepository<Reserva, UUID> {

    @Query("SELECT COUNT(rd) FROM Reserva r " +
            "JOIN r.actividadDia ad " +
            "JOIN r.reservaDetalles rd " +
            "WHERE ad.id = :uuid " +
            "AND r.estadoActual.estadoReserva.nombre " +
            "IN (com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE, com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA)")
    Long getCuposReservadosActividadDia(@Param("uuid") UUID uuidActividadDia);

    @Query("SELECT er FROM EstadoReserva er " +
            "WHERE er.nombre = :reservaEstadoNombre")
    Optional<EstadoReserva> findEstadoReservaByEstadoReservaNombre(@Param("reservaEstadoNombre") EstadoReservaNombre reservaEstadoNombre);

    @Query("SELECT DISTINCT r FROM Reserva r " +
            "LEFT JOIN FETCH r.estados " +
            "JOIN r.estadoActual estado " +
            "WHERE estado.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE " +
            "AND r.fechaHoraExpiracion < :currTime")
    List<Reserva> findReservasExpiradas(@Param("currTime")LocalDateTime currTime);

    @Query("select r from Reserva r where r.visitante.id = :visitanteId and r.estadoActual.estadoReserva.id = :estadoId")
    List<Reserva> findByVisitanteAndReservaEstadoActual(@Param("visitanteId") UUID visitanteId, @Param("estadoId") UUID estadoReservaId);

    @Query("SELECT DISTINCT r FROM Reserva r " +
            "JOIN FETCH r.actividad " +
            "JOIN FETCH r.actividadDia " +
            "JOIN FETCH r.reservaDetalles " +
            "WHERE r.visitante.id = :visitanteId")
    List<Reserva> findByVisitanteId(@Param("visitanteId") UUID visitante);

    @Query("SELECT DISTINCT r FROM Reserva r " +
            "LEFT JOIN FETCH r.estados " +
            "JOIN r.estadoActual estado " +
            "WHERE estado.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE")
    List<Reserva> findReservasPendientes(@Param("currTime") LocalDateTime currTime);


    @Query("SELECT r FROM Reserva r " +
            "JOIN FETCH r.pago p " +
            "WHERE p.idPagoExterno = :idPagoExterno ")
    Optional<Reserva> findByPagoWithIdPagoExterno(@Param("idPagoExterno") String idPagoExterno);

    @Query("SELECT COUNT(r) > 0 FROM Reserva r " +
            "WHERE r.visitante.id = :visitanteId " +
            "AND r.estadoActual.estadoReserva.nombre = :estadoReservaNombre ")
    boolean tieneReservasEnEstadoByVisitanteId(UUID visitanteId, EstadoReservaNombre estadoReservaNombre);

    @Query("SELECT r FROM Reserva r " +
            "JOIN FETCH r.visitante v " +
            "JOIN FETCH r.actividadDia ad " +
            "WHERE v.id = :visitanteId " +
            "AND ad.id = :adId " +
            "AND r.estadoActual.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE ")
    Optional<Reserva> findByVisitanteIdAndActividadDiaId(@Param("visitanteId") UUID visitanteId, @Param("adId") UUID actividadDiaId);

    @Query("select new com.mza_agrotours.backend.dtos.administrador_sistemas.ConteoPorEstablecimientoDTO(r.actividad.establecimiento.id, count(r)) " +
            "from Reserva r " +
            "where r.actividad.establecimiento.id in :ids " +
            "group by r.actividad.establecimiento.id")
    List<ConteoPorEstablecimientoDTO> countReservasTotalesByEstablecimientoIds(@Param("ids") Set<UUID> establecimientoIds);

    @Query("SELECT new com.mza_agrotours.backend.dtos.actividad.DTOReservasBloqueantes(" +
            "  COUNT(CASE WHEN r.estadoActual.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE THEN r.id END), " +
            "  COUNT(CASE WHEN r.estadoActual.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA THEN r.id END)) " +
            "FROM Reserva r " +
            "WHERE r.actividad.id = :actividadId " +
            "AND r.estadoActual.estadoReserva.nombre IN (" +
            "  com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE, " +
            "  com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA) " +
            "AND r.actividadDia.fechaHoraInicio > :ahora")
    DTOReservasBloqueantes contarReservasBloqueantes(@Param("actividadId") UUID actividadId,
                                                     @Param("ahora") LocalDateTime ahora);

    @Query("SELECT new com.mza_agrotours.backend.dtos.actividad.DTOConteoReservasPorActividad(" +
            " r.actividad.id, COUNT(r.id)) " +
            "FROM Reserva r " +
            "WHERE r.actividad.id IN :actividadIds " +
            "AND r.estadoActual.estadoReserva.nombre IN (" +
            "  com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE, " +
            "  com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA) " +
            "AND r.actividadDia.fechaHoraInicio > :ahora " +
            "GROUP BY r.actividad.id")
    List<DTOConteoReservasPorActividad> contarReservasBloqueantesPorActividad(
            @Param("actividadIds") List<UUID> actividadIds,
            @Param("ahora") LocalDateTime ahora);

    @Query("SELECT r FROM Reserva r " +
            "LEFT JOIN FETCH r.pago " +
            "JOIN FETCH r.visitante v " +
            "JOIN FETCH v.usuario " +
            "JOIN FETCH r.actividadDia " +
            "WHERE r.actividad.id = :actividadId " +
            "AND r.estadoActual.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE")
    List<Reserva> findPendientesByActividadId(@Param("actividadId") UUID actividadId);

    @Query("SELECT COUNT(r) > 0 FROM Reserva r " +
            "WHERE r.actividad.id = :actividadId " +
            "AND r.estadoActual.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA " +
            "AND r.actividadDia.fechaHoraInicio > :ahora")
    boolean existeReservaPagadaFuturaByActividadId(@Param("actividadId") UUID actividadId,
                                                   @Param("ahora") LocalDateTime ahora);

    @Query("SELECT new com.mza_agrotours.backend.dtos.actividad.DTOCuposPorDia(" +
            "  ad.id, " +
            "  COUNT(CASE WHEN r.estadoActual.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE THEN rd.id END), " +
            "  COUNT(CASE WHEN r.estadoActual.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA THEN rd.id END)) " +
            "FROM Reserva r " +
            "JOIN r.actividadDia ad " +
            "JOIN r.reservaDetalles rd " +          // una fila por persona
            "WHERE ad.id IN :diaIds " +
            "AND r.estadoActual.estadoReserva.nombre IN (" +
            "  com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE, " +
            "  com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA) " +
            "GROUP BY ad.id")
    List<DTOCuposPorDia> contarCuposPorDia(@Param("diaIds") List<UUID> diaIds);

    @Query("SELECT new com.mza_agrotours.backend.dtos.actividad.DTOMetricasReservasGlobales(" +
            "  COUNT(CASE WHEN r.estadoActual.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE THEN rd.id END), " +
            "  COUNT(CASE WHEN r.estadoActual.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA THEN rd.id END), " +
            "  COUNT(CASE WHEN r.estadoActual.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.FINALIZADA THEN rd.id END)) " +
            "FROM Reserva r " +
            "JOIN r.reservaDetalles rd " +          // una fila por persona
            "WHERE r.actividad.id = :actividadId")
    DTOMetricasReservasGlobales obtenerMetricasDeReservas(@Param("actividadId") UUID actividadId);
}
