package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.solicitud_establecimiento.EstadoSolicitudEstablecimiento;

import java.util.UUID;

public interface EstadoSolicitudEstablecimientoRepository extends BaseEntityRepository<EstadoSolicitudEstablecimiento, UUID> {
    boolean existsByNombre(String nombre);
}
