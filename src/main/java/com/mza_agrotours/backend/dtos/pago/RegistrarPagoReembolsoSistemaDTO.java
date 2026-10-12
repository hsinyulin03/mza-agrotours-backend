package com.mza_agrotours.backend.dtos.pago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RegistrarPagoReembolsoSistemaDTO(
        // Fecha y hora en que el administrador hizo la transacción
        @NotNull @PastOrPresent LocalDateTime fechaHoraPago,

        // Medio con el que se pagó realmente, texto libre
        @NotBlank @Size(max = 64) String metodoPago,

        // ID de la transacción en el medio de pago usado
        @NotBlank @Size(max = 64) String idPagoExterno
) {

}
