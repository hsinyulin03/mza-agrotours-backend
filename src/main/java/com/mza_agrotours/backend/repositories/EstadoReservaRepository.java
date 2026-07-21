package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadoReservaRepository extends BaseEntityRepository<EstadoReserva, UUID> {
    Optional<EstadoReserva> findByNombre(EstadoReservaNombre nombre);
}
