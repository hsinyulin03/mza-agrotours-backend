package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.SolicitudEstablecimiento;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SolicitudEstablecimientoRepository extends BaseEntityRepository<SolicitudEstablecimiento, UUID> {

    boolean existsByUsuario_IdAndEstadoActual_EstadoSolicitudEstablecimiento_IdAndCuit(
            @Param("usuarioId") UUID usuarioId,
            @Param("estadoActualId") UUID estadoActualId,
            @Param("cuit") String cuit);

    List<SolicitudEstablecimiento> findAllByUsuario(Usuario usuario);
}
