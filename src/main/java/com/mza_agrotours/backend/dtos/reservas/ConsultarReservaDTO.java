package com.mza_agrotours.backend.dtos.reservas;

import java.util.Objects;

public record DetalleReservaDTO (
    // Reserva - total, idReserva(?)
    Float totalReserva, Long idReserva,
    // ReservaEstado - estado
    String estadoReserva,
    // ReservaDetalle - cantidad, [nombre, tipo rango etario, subtotal]
    Integer cantPersonas,
    
    // ActividadDia - día, hInicio, hFin
    // Actividad - nombre, ubicación, URL A LA ACTIVIDAD TODO - ubicación es la del establecimiento?. BTW cómo guardamos la ubi para que aparezca en el mapa?
    // Establecimiento - URL AL ESTABLECIMIENTO
    // Foto - url, nombre
) {
}
