package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.establecimiento.EstablecimientoEstado;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EstablecimientoEstadoRepository
        extends BaseEntityRepository<EstablecimientoEstado, UUID> {
}