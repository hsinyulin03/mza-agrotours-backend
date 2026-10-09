package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DTOActividadDiaUpdateCupo {
    @NotNull(message = "El cupo máximo es requerido")
    @Min(value = 1, message = "El cupo máximo debe ser mayor a 0")
    private Integer cuposMax;
}
