package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.deudas.EstadoDeudaEstablecimiento;
import com.mza_agrotours.backend.enums.EstadoDeudaEstablecimientoNombre;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadoDeudaEstablecimientoRepository extends BaseEntityRepository<EstadoDeudaEstablecimiento, UUID>{

    Optional<EstadoDeudaEstablecimiento> findByNombre(EstadoDeudaEstablecimientoNombre nombre);
    boolean existsByNombre(EstadoDeudaEstablecimientoNombre nombre);
}