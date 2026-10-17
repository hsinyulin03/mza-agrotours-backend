package com.mza_agrotours.backend.dtos.deudas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DeudaPagoDTO(
        // Pago - id, fecha y hora de la transacción, id externo, monto
        String idPago, LocalDateTime fechaHoraPago, String idPagoExterno, BigDecimal monto,

        // DeudaPago - medio con el que se pagó realmente y cuándo se cargó
        String metodoPago, LocalDateTime fechaHoraCarga,

        // Nombre del administrador que lo cargó
        String administrador
) {
}
