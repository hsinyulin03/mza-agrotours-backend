package com.mza_agrotours.backend.repositories.pago;

import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.EstadoPagoNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadoPagoRepository extends BaseEntityRepository<EstadoPago, UUID> {
    Optional<EstadoPago> findByNombre(EstadoPagoNombre nombre);
    boolean existsByNombre(EstadoPagoNombre nombre);
}