package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.dtos.reservas.DiaActividadReservaDTO;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaRepository extends BaseEntityRepository<Reserva, UUID> {

    @Query("SELECT COUNT(r) FROM Reserva r " +
            "JOIN r.actividadDia ad " +
            "WHERE ad.id = :uuid " +
            "AND r.estadoActual.estadoReserva.nombre " +
            "IN (com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre.PENDIENTE, com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre.PAGADA)")
    Integer getCantidadReservasActivasActividadDia(@Param("uuid") UUID uuidActividadDia);

    @Query("SELECT er FROM EstadoReserva er " +
            "JOIN er.nombre nom " +
            "WHERE nom = :reservaEstadoNombre")
    Optional<EstadoReserva> findEstadoReservaByEstadoReservaNombre(@Param("reservaEstadoNombre") EstadoReservaNombre reservaEstadoNombre);

    @Query("SELECT DISTINCT r FROM Reserva r " +
            "LEFT JOIN FETCH r.estados " +
            "JOIN r.estadoActual estado " +
            "WHERE estado.estadoReserva.nombre = com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre.PENDIENTE " +
            "AND r.fechaHoraExpiracion < :currTime")
    List<Reserva> findReservasExpiradas(@Param("currTime")LocalDateTime currTime);
}
