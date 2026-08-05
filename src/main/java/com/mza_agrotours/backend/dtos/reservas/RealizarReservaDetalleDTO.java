package com.mza_agrotours.backend.dtos.reservas;

import com.mza_agrotours.backend.validation.EdadMaxima;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RealizarReservaDetalleDTO(
        @NotBlank
        @Size(min = 3, max = 40)
        String nombreApellido,


        @NotBlank
        @Size(min = 1, max = 20)
        String identificacion,

        @NotBlank
        String tipoIdentificacion,

        @NotNull
        @Past
        @EdadMaxima(120)
        LocalDate fechaNacimiento
) {
}
