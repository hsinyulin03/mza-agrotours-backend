package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.dtos.actividad.DTOConteoReservasPorActividad;
import com.mza_agrotours.backend.dtos.actividad.DTOReservasBloqueantes;
import com.mza_agrotours.backend.dtos.actividad.DTOCuposPorDia;
import com.mza_agrotours.backend.dtos.actividad.DTOMetricasReservasGlobales;
import com.mza_agrotours.backend.dtos.administrador_sistemas.ConteoPorEstablecimientoDTO;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    @Query("SELECT DISTINCT r FROM Reserva r " +
            "LEFT JOIN FETCH r.estados " +
            "JOIN r.estadoActual estado " +
            "JOIN r.actividadDia ad " +
            "WHERE estado.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA " +
            "AND ad.estadoActual.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadDiaNombre.FINALIZADA")
    List<Reserva> findReservasPagadasDeDiasFinalizados();

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
            "WHERE p.idCheckoutExterno = :idCheckoutExterno ")
    Optional<Reserva> findByPagoWithIdCheckoutExterno(@Param("idCheckoutExterno") String idCheckoutExterno);

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

    @Query("SELECT r.id FROM Reserva r " +
            "WHERE r.actividad.id = :actividadId " +
            "AND r.actividadDia.id = :actividadDiaId " +
            "AND r.estadoActual.estadoReserva.nombre IN :estados")
    Page<UUID> findIdsReservasDelDiaParaProductor(@Param("actividadId") UUID actividadId,
                                                  @Param("actividadDiaId") UUID actividadDiaId,
                                                  @Param("estados") List<EstadoReservaNombre> estados,
                                                  Pageable pageable);

    @Query("SELECT DISTINCT r FROM Reserva r " +
            "JOIN FETCH r.estadoActual ea " +
            "JOIN FETCH ea.estadoReserva " +
            "JOIN FETCH r.reservaDetalles rd " +
            "JOIN FETCH rd.actividadRangoEtario " +
            "WHERE r.id IN :ids")
    List<Reserva> findReservasConDetallesByIds(@Param("ids") List<UUID> ids);

    @Query("SELECT COUNT(r) FROM Reserva r " +
            "WHERE r.actividadDia.id = :actividadDiaId " +
            "AND r.estadoActual.estadoReserva.nombre IN :estados")
    long contarReservasDelDia(@Param("actividadDiaId") UUID actividadDiaId,
                              @Param("estados") List<EstadoReservaNombre> estados);

    @Query("SELECT COALESCE(SUM(r.totalReserva), 0) FROM Reserva r " +
            "WHERE r.actividadDia.id = :actividadDiaId " +
            "AND r.estadoActual.estadoReserva.nombre IN :estados")
    BigDecimal sumarIngresoDelDia(@Param("actividadDiaId") UUID actividadDiaId,
                                  @Param("estados") List<EstadoReservaNombre> estados);

    //Recordatorio: reservas pagadas cuyo día empieza dentro de la ventana y que todavía no recibieron el recordatorio
    @Query("SELECT r.id FROM Reserva r " +
            "WHERE r.estadoActual.estadoReserva.nombre = com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA " +
            "AND r.fechaHoraRecordatorio IS NULL " +
            "AND r.actividadDia.fechaHoraInicio > :desde " +
            "AND r.actividadDia.fechaHoraInicio <= :hasta")
    List<UUID> findIdsRecordatorioPendiente(@Param("desde") LocalDateTime desde,
                                            @Param("hasta") LocalDateTime hasta);


    @Query("SELECT COUNT(rd) FROM Reserva r " +
            "JOIN r.reservaDetalles rd " +          // una fila por persona
            "WHERE r.actividad.establecimiento.id = :establecimientoId " +
            "AND r.estadoActual.estadoReserva.nombre IN :estados " +
            "AND r.actividadDia.fechaHoraInicio >= :desde " +
            "AND r.actividadDia.fechaHoraInicio < :hasta")
    long contarCuposEnRango(@Param("establecimientoId") UUID establecimientoId,
                            @Param("estados") List<EstadoReservaNombre> estados,
                            @Param("desde") LocalDateTime desde,
                            @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COALESCE(SUM(r.subTotalProductor), 0) FROM Reserva r " +
            "WHERE r.actividad.establecimiento.id = :establecimientoId " +
            "AND r.estadoActual.estadoReserva.nombre IN :estados " +
            "AND r.actividadDia.fechaHoraInicio >= :desde " +
            "AND r.actividadDia.fechaHoraInicio < :hasta")
    BigDecimal sumarSubTotalProductorEnRango(@Param("establecimientoId") UUID establecimientoId,
                                             @Param("estados") List<EstadoReservaNombre> estados,
                                             @Param("desde") LocalDateTime desde,
                                             @Param("hasta") LocalDateTime hasta);

    //Estadísticas: cantidad de reservas del establecimiento en los estados dados, cuyo día de actividad cae dentro del rango
    @Query("SELECT COUNT(r) FROM Reserva r " +
            "WHERE r.actividad.establecimiento.id = :establecimientoId " +
            "AND r.estadoActual.estadoReserva.nombre IN :estados " +
            "AND r.actividadDia.fechaHoraInicio >= :desde " +
            "AND r.actividadDia.fechaHoraInicio < :hasta")
    long contarReservasEnRango(@Param("establecimientoId") UUID establecimientoId,
                               @Param("estados") List<EstadoReservaNombre> estados,
                               @Param("desde") LocalDateTime desde,
                               @Param("hasta") LocalDateTime hasta);

    //Estadísticas: fecha de inicio del día y subTotalProductor de cada reserva del establecimiento en el rango, para armar la serie del gráfico
    @Query("SELECT r.actividadDia.fechaHoraInicio, r.subTotalProductor FROM Reserva r " +
            "WHERE r.actividad.establecimiento.id = :establecimientoId " +
            "AND r.estadoActual.estadoReserva.nombre IN :estados " +
            "AND r.actividadDia.fechaHoraInicio >= :desde " +
            "AND r.actividadDia.fechaHoraInicio < :hasta")
    List<Object[]> findFechaYSubTotalProductorEnRango(@Param("establecimientoId") UUID establecimientoId,
                                                      @Param("estados") List<EstadoReservaNombre> estados,
                                                      @Param("desde") LocalDateTime desde,
                                                      @Param("hasta") LocalDateTime hasta);



    @Query("SELECT YEAR(r.actividadDia.fechaHoraInicio), MONTH(r.actividadDia.fechaHoraInicio), " +
            "COUNT(r), COALESCE(SUM(r.subTotalProductor), 0) " +
            "FROM Reserva r " +
            "WHERE r.actividad.establecimiento.id = :establecimientoId " +
            "AND r.estadoActual.estadoReserva.nombre IN :estados " +
            "AND r.actividadDia.fechaHoraInicio >= :desde " +
            "AND r.actividadDia.fechaHoraInicio < :hasta " +
            "GROUP BY YEAR(r.actividadDia.fechaHoraInicio), MONTH(r.actividadDia.fechaHoraInicio)")
    List<Object[]> contarYSumarPorMesEnRango(@Param("establecimientoId") UUID establecimientoId,
                                             @Param("estados") List<EstadoReservaNombre> estados,
                                             @Param("desde") LocalDateTime desde,
                                             @Param("hasta") LocalDateTime hasta);
    // Ingresos por actividad
    @Query("SELECT r.actividad.id, COALESCE(SUM(r.totalReserva), 0) FROM Reserva r " +
            "WHERE r.actividad.establecimiento.id = :establecimientoId " +
            "AND r.estadoActual.estadoReserva.nombre IN :estados " +
            "AND r.actividadDia.fechaHoraInicio >= :desde " +
            "AND r.actividadDia.fechaHoraInicio < :hasta " +
            "GROUP BY r.actividad.id")
    List<Object[]> sumarIngresosEnRangoPorActividad(@Param("establecimientoId") UUID establecimientoId,
                                                    @Param("estados") List<EstadoReservaNombre> estados,
                                                    @Param("desde") LocalDateTime desde,
                                                    @Param("hasta") LocalDateTime hasta);

    // Cupos reservados (personas) por actividad
    @Query("SELECT r.actividad.id, COUNT(rd) FROM Reserva r " +
            "JOIN r.reservaDetalles rd " +          // una fila por persona
            "WHERE r.actividad.establecimiento.id = :establecimientoId " +
            "AND r.estadoActual.estadoReserva.nombre IN :estados " +
            "AND r.actividadDia.fechaHoraInicio >= :desde " +
            "AND r.actividadDia.fechaHoraInicio < :hasta " +
            "GROUP BY r.actividad.id")
    List<Object[]> contarCuposEnRangoPorActividad(@Param("establecimientoId") UUID establecimientoId,
                                                  @Param("estados") List<EstadoReservaNombre> estados,
                                                  @Param("desde") LocalDateTime desde,
                                                  @Param("hasta") LocalDateTime hasta);

}
