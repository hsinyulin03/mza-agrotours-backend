package com.mza_agrotours.backend.dtos.reservas;

import org.springframework.data.util.Pair;

import java.util.List;

public record ConsultarReservaDTO(
        // Reserva - total, idReserva(?)
        Float totalReserva, java.util.UUID idReserva,

        // ReservaEstado - estado
        String estadoReserva,

        // ReservaDetalle - cantidad, [renglón, nombre, tipo rango etario, subtotal]
        Integer cantPersonas,
        List<ConsultarReservaDetalleDTO> detalleDTOs,

        // ActividadDia - día, hInicio, hFin
        String diaActividad, String horaInicio, String HoraFin,

        // Actividad - nombre, ubicación, URL A LA ACTIVIDAD    TODO - ubicación es la del establecimiento?. BTW cómo guardamos la ubi para que aparezca en el mapa?
        String nombreActividad, String urlActividad, String ubicacionActividad,

        // Establecimiento - nombre, URL AL ESTABLECIMIENTO
        String nombreEstablecimiento, String urlEstablecimiento,

        // Fotos - [url, nombre]    TODO - TEMPORAL. Poner PAIR acá es una mugre
        List<Pair<String, String>> fotos
) {

}
