package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DTOTarifa {
    @NotNull(message = "El rango etario es requerido")
    private UUID rangoEtarioId;

    @NotNull(message = "Este campo es requerido")
    @DecimalMin(value = "0.1" ,message = "El precio debe ser mayor a 0")
    private BigDecimal precio;
}
