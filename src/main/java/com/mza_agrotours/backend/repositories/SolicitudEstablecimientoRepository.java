package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.solicitud_establecimiento.SolicitudEstablecimiento;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SolicitudEstablecimientoRepository extends BaseEntityRepository<SolicitudEstablecimiento, UUID> {

    boolean existsByUsuario_IdAndEstadoActual_EstadoSolicitudEstablecimiento_IdAndRazonSocial(
            @Param("usuarioId") UUID usuarioId,
            @Param("estadoActualId") UUID estadoActualId,
            @Param("razonSocial") String razonSocial);
}
