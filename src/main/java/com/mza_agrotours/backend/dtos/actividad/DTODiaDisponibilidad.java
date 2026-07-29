package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.enums.Dia;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class DTODiaDisponibilidad {
    @NotNull(message = "El día es requerido")
    private Dia dia;

    @NotNull(message = "La hora de inicio es requerida")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es requerida")
    private LocalTime horaFin;
}
