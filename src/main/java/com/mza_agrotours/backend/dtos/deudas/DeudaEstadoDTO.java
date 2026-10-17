package com.mza_agrotours.backend.dtos.deudas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DeudaEstadoDTO(
        String nombre,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        BigDecimal montoDevueltoTransaccion,
        String motivo,
        DeudaPagoDTO pago // Null si el cambio no tuvo pago
) {
}
