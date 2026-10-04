package com.mza_agrotours.backend.dtos.calificacion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CalificacionResponseDTO {
    private UUID idCalificacion;
    private UUID idReserva;
    private Integer puntaje;
    private String resenia;
    private LocalDateTime fechaHoraCalificacion;
}