package com.mza_agrotours.backend.dtos;

import com.mza_agrotours.backend.entities.Parametros;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ParametrosDTO(

        @NotNull
        @Positive
        Integer diasMaxCrearActividad,

        @NotNull
        @PositiveOrZero
        Integer diasMinReembolso,

        @NotNull
        @Positive
        Integer ttlReserva,         // En minutos

        @NotNull
        @DecimalMin("0")
        @DecimalMax("1")
        @Digits(integer = 1, fraction = 4)
        BigDecimal porcentajeComision,  // Fracción: 0.1 = 10%

        @NotNull
        @Positive
        Integer diasImpagoReserva // Días que pasan hasta que una reserva que el productor debe pagar pase a impaga y sea pagada por un administrador de sistemas
) {
    public static ParametrosDTO from(Parametros parametros) {
        return new ParametrosDTO(
                parametros.getDiasMaxCrearActividad(),
                parametros.getDiasMinReembolso(),
                parametros.getTtlReserva(),
                parametros.getPorcentajeComision(),
                parametros.getDiasImpagoReserva()
        );
    }
}
