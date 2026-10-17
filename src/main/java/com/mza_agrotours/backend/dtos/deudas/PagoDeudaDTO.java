package com.mza_agrotours.backend.dtos.deudas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoDeudaDTO(
        // Fecha y hora en que el establecimiento hizo la transacción
        @NotNull @PastOrPresent LocalDateTime fechaHoraPago,

        // Monto devuelto en esta transacción
        @NotNull @Positive BigDecimal monto,

        // Medio con el que se pagó realmente, texto libre
        @NotBlank @Size(max = 64) String metodoPago,

        // ID de la transacción en el medio de pago usado
        @NotBlank @Size(max = 64) String idPagoExterno
) {
}
