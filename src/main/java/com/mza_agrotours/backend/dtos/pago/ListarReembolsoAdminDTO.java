package com.mza_agrotours.backend.dtos.pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ListarReembolsoAdminDTO(
        // Reembolso - id, fechaHoraPedido, monto
        String idReembolso, LocalDateTime fechaHoraPedido, BigDecimal montoReembolso,

        // ReembolsoEstado - estado actual y desde cuándo está en él
        String estado, LocalDateTime fechaHoraEstado,

        // Establecimiento - nombre y nombre del productor líder (titular)
        String nombreEstablecimiento, String nombreProductorLider,

        // Deuda que generó el reembolso al pagarlo el sistema. Null mientras está IMPAGO
        String idDeuda
) {

}
