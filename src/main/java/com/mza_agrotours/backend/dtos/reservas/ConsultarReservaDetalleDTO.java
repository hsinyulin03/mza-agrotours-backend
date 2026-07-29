package com.mza_agrotours.backend.dtos.reservas;

import java.math.BigDecimal;

public record ConsultarReservaDetalleDTO(
        // ReservaDetalle - renglón, nombre, tipo rango etario, subtotal
        Integer renglon,
        String nombre,
        String tipoRangoEtario,
        BigDecimal subtotal
) {

}