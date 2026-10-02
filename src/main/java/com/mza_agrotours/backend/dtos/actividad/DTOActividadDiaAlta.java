package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DTOActividadDiaAlta {
    @NotNull(message = "La fecha es requerida")
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es requerida")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es requerida")
    private LocalTime horaFin;

    @NotNull(message = "El cupo máximo es requerido")
    @Min(value = 1, message = "El cupo máximo debe ser mayor a 0")
    private Integer cuposMax;
}
