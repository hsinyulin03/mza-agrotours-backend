package com.mza_agrotours.backend.dtos.reservas;

import java.time.LocalDateTime;

public record DiaActividadReservaDTO(
        String id,
        Integer cuposMax,
        Integer cuposOcupados,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin
) {

}
