package com.mza_agrotours.backend.dtos.tipoCultivo;

import com.mza_agrotours.backend.enums.UnidadNutricional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DTOTipoCultivoInfoNutriAM {
    @NotBlank(message = "El nombre del nutriente es requerido")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String nombre;

    @NotBlank(message = "El valor nutricional es requerido")
    @Size(max = 30, message = "Máximo 30 caracteres")
    private String valor;

    @NotNull(message = "La unidad es requerida")
    private UnidadNutricional unidad;
}
