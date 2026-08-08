package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.dtos.productor.EstadoProductor;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;

import java.util.Optional;
import java.util.UUID;

public interface EstadoProductorRepository extends BaseEntityRepository<EstadoProductor, UUID> {
    Optional<EstadoProductor> findByNombreAndFechaHoraBajaIsNull(EstadoProductorNombre nombre);
}
