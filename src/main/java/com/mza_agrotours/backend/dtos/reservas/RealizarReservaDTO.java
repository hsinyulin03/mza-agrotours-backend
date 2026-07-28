package com.mza_agrotours.backend.dtos.reservas;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record RealizarReservaDTO(
        @NotBlank
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "diaActividadId debe ser un UUID válido")
        String diaActividadId,

        @NotEmpty
        @Valid
        List<RealizarReservaDetalleDTO> reservaDetalleList
) {
}
