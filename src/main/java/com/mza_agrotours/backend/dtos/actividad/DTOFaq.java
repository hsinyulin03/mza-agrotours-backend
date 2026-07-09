package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DTOFaq {
    @NotBlank(message = "La pregunta no puede estar vacía")
    @Size(min = 5, max = 200, message = "La pregunta debe tener entre 5 y 200 caracteres")
    private String pregunta;

    @NotBlank(message = "La respuesta no puede estar vacía")
    @Size(min = 5, max = 200, message = "La respuesta debe tener entre 5 y 200 caracteres")
    private String respuesta;
}
