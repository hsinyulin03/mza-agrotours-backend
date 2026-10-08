package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.enums.Dia;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
public class DTOActividadDiasLote {
    @NotNull(message = "La fecha desde es requerida")
    private LocalDate fechaDesde;

    @NotNull(message = "La fecha hasta es requerida")
    private LocalDate fechaHasta;

    @NotEmpty(message = "Debe seleccionar al menos un día de la semana")
    private Set<Dia> dias;

    @NotNull(message = "La hora de inicio es requerida")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es requerida")
    private LocalTime horaFin;

    @NotNull(message = "El cupo máximo es requerido")
    @Min(value = 1, message = "El cupo máximo debe ser mayor a 0")
    private Integer cuposMax;
}
