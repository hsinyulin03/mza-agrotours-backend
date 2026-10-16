package com.mza_agrotours.backend.dtos.reservas;

import com.mza_agrotours.backend.dtos.archivo.DTOFotosResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ListarReservaDTO(
        // Reserva - total, idReserva, fechaHoraInicio
        BigDecimal totalReserva, String idReserva, LocalDateTime fechaHoraInicioReserva,

        // ReservaEstado - estado
        String estadoReserva,

        Integer cantPersonas,

        // ActividadDia - fechaHoraInicio, fechaHoraFin
        LocalDateTime actividadFechaHoraInicio, LocalDateTime actividadFechaHoraFin,

        // Actividad - nombre, ubicación, id
        String nombreActividad, String idActividad,

        // Establecimiento - nombre, id, ubicación
        String nombreEstablecimiento, String ubicacionEstablecimiento,

        DTOFotosResponse fotoPortada
) {

}
