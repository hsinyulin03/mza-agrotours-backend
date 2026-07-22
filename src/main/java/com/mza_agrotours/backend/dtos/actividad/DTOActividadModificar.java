package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;


@Data
public class DTOActividadModificar {
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 5, max = 80, message = "El nombre debe tener entre 5 y 80 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9 áéíóúÁÉÍÓÚñÑ]+$", message = "No se aceptan caracteres especiales")
    private String nombre;

    @NotBlank(message = "La descripción es requerida")
    @Size(min = 20, max = 2000, message = "La descripción debe tener entre 20 y 2000 caracteres")
    private String descripcion;

    @NotNull(message = "Este campo es obligatorio")
    @Min(value = 1, message = "El cupo máximo debe ser mayor a 0")
    private int cuposMax;

    @Valid
    @NotEmpty(message = "Debe configurar al menos la tarifa base")
    private List<DTOTarifa> tarifas;

    private List<@Size(min = 5, max = 200, message = "El ítem debe tener entre 5 y 200 caracteres") String> incluye;

    private List<@Size(min = 5, max = 200, message = "El ítem debe tener entre 5 y 200 caracteres") String> noIncluye;

    @Valid
    private List<DTOFaq> faqs;

    @NotNull(message = "El estado de la actividad es requerido")
    private String estado; // Enum: BORRADOR o PUBLICADO
}
