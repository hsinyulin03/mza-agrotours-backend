package com.mza_agrotours.backend.repositories.pago;

import com.mza_agrotours.backend.entities.pago.EstadoReembolso;
import com.mza_agrotours.backend.enums.EstadoReembolsoNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadoReembolsoRepository extends BaseEntityRepository<EstadoReembolso, UUID> {
    Optional<EstadoReembolso> findByNombre(EstadoReembolsoNombre nombre);
    boolean existsByNombre(EstadoReembolsoNombre nombre);
}