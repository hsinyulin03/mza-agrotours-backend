package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DTOCambioEstadoActividad {
    @NotBlank(message = "El estado es obligatorio")
    private String estado; // BORRADOR o PUBLICADO
}
