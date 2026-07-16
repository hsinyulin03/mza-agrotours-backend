package com.mza_agrotours.backend.dtos.rangoEtario;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DTORangoEtarioAlta {
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 3, max = 40, message = "El nombre debe tener entre 3 y 40 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]+$", message = "No se aceptan caracteres especiales")
    private String nombre;

    @NotNull(message = "Este campo es obligatorio")
    @Min(value = 0, message = "La edad mínima no puede ser un número negativo")
    private Integer edadMinima;

    @NotNull(message = "Este campo es obligatorio")
    @Max(value = 120, message = "La edad máxima debe ser menor a 120")
    private Integer edadMaxima;

}