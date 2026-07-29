package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DTOTarifa {
    @NotBlank(message = "El nombre del rango es obligatorio")
    @Size(min = 3, max = 40, message = "El nombre debe tener entre 3 y 40 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9 áéíóúÁÉÍÓÚñÑ]+$", message = "No se aceptan caracteres especiales")
    private String nombre;

    @NotNull(message = "Este campo es requerido")
    @DecimalMin(value = "0.1" ,message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    @NotNull(message = "Este campo es obligatorio")
    @Min(value = 0, message = "La edad mínima no puede ser un número negativo")
    @Max(value = 120, message = "La edad mínima no puede ser mayor a 120")
    private Integer edadMinima;

    @NotNull(message = "Este campo es obligatorio")
    @Min(value = 0, message = "La edad máxima no puede ser un número negativo")
    @Max(value = 120, message = "La edad máxima no puede ser mayor a 120")
    private Integer edadMaxima;

    private boolean esTarifaBase;
}
