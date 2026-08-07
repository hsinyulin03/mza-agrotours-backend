package com.mza_agrotours.backend.dtos.receta;


import com.mza_agrotours.backend.enums.Dificultad;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DTORecetaAM {

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @NotEmpty(message = "Agregá al menos un cultivo")
    private List<UUID> cultivosIds;

    @NotNull(message = "La dificultad es requerida")
    private Dificultad dificultad;

    @NotNull(message = "El tiempo aproximado en minutos es requerido")
    @Positive(message = "Debe ser mayor a 0")
    private Integer tiempoMinsAprox;

    @NotNull(message = "Las porciones son requeridas")
    @Positive(message = "Debe ser mayor a 0")
    private Integer porciones;

    @NotBlank(message = "La descripción es requerida")
    @Size(max = 500, message = "Máximo 500 caracteres")
    private String descripcion;

    @NotEmpty(message = "Cargá al menos un ingrediente")
    private List<@NotBlank @Size(max = 100) String> ingredientes;

    @NotEmpty(message = "Cargá al menos un paso")
    private List<@NotBlank @Size(max = 200) String> pasos;
}