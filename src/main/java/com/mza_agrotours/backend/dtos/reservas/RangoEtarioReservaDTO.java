package com.mza_agrotours.backend.dtos.reservas;

import java.math.BigDecimal;

public record RangoEtarioReservaDTO(
        BigDecimal precio,
        String nombre,
        Integer edadMinima,
        Integer edadMaxima
) {
}
