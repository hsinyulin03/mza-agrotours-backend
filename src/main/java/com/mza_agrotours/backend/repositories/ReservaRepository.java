package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.reservas.Reserva;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservaRepository extends BaseEntityRepository<Reserva, UUID> {
    @Query("select r from Reserva r where r.visitante.id = :visitanteId and r.estadoActual.estadoReserva.id = :estadoId")
    List<Reserva> findByVisitanteAndReservaEstadoActual(@Param("visitanteId") UUID visitanteId, @Param("estadoId") UUID estadoReservaId);
}
