package com.mza_agrotours.backend.dtos.incidencia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IncidenciaCreateRequest {
    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 1, max = 50, message = "El título debe tener entre 1 y 50 caracteres")
    private String titulo;
    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(min=1, max=1000, message = "La descripción debe tener entre 1 y 1000 caracteres")
    private String descripcion;
}
