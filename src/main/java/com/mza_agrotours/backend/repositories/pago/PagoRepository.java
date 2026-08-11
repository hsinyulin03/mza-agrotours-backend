package com.mza_agrotours.backend.repositories.pago;

import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.EstadoPagoNombre;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PagoRepository extends BaseEntityRepository<Pago, UUID> {

    @Query("SELECT ep FROM EstadoPago ep " +
            "WHERE ep.nombre = :estadoPagoNombre")
    Optional<EstadoPago> findEstadoPagoByEstadoPagoNombre(@Param("estadoPagoNombre") EstadoPagoNombre estadoPagoNombre);
}