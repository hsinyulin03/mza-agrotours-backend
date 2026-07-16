package com.mza_agrotours.backend.dtos.reservas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ConsultarReservaDTO(
        // Reserva - total, idReserva(?)
        BigDecimal totalReserva, String idReserva,

        // ReservaEstado - estado
        String estadoReserva,

        // ReservaDetalle - cantidad, [renglón, nombre, tipo rango etario, subtotal]
        Integer cantPersonas,
        List<ConsultarReservaDetalleDTO> detalleDTOs,

        // ActividadDia - fechaHoraInicio, fechaHoraFin
        LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin,

        // Actividad - nombre, ubicación, id
        String nombreActividad, String idActividad,

        // Establecimiento - nombre, id, ubicación
        String nombreEstablecimiento, String idEstablecimiento, String ubicacionEstablecimiento

        // TODO Fotos - [url, nombre]
        // List<FotoDTO> fotos
) {

}
