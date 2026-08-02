package com.mza_agrotours.backend.dtos.reservas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ListarReservaDTO(
        // Reserva - total, idReserva(?)
        BigDecimal totalReserva, String idReserva,

        // ReservaEstado - estado
        String estadoReserva,

        Integer cantPersonas,

        // ActividadDia - fechaHoraInicio, fechaHoraFin
        LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin,

        // Actividad - nombre, ubicación, id
        String nombreActividad, String idActividad,

        // Establecimiento - nombre, id, ubicación
        String nombreEstablecimiento, String ubicacionEstablecimiento

        // TODO Foto - (url, nombre)
        // FotoDTO foto
) {

}
