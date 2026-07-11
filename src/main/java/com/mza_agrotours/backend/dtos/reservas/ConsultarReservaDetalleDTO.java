package com.mza_agrotours.backend.dtos.reservas;

public record ConsultarReservaDetalleDTO(
        // ReservaDetalle - renglón, nombre, tipo rango etario, subtotal
        Integer renglon,
        String nombre,
        String tipoRangoEtario,
        Float subtotal
) {

}