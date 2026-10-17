package com.mza_agrotours.backend.dtos.deudas;

import com.mza_agrotours.backend.enums.TipoCambioEstadoDeuda;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoDeudaDTO(
        @NotNull TipoCambioEstadoDeuda tipo,

        @NotBlank @Size(max = 255) String motivo,

        // Obligatorio en DEVOLUCION, opcional en ACUERDO_MEDIO
        @Valid PagoDeudaDTO pago
) {
}
