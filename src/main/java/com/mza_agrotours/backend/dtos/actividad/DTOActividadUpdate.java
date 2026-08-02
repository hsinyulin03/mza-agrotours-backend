package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.validation.SinCaracteresEspeciales;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class DTOActividadUpdate {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 5, max = 80, message = "El nombre debe tener entre 5 y 80 caracteres")
    @SinCaracteresEspeciales
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 20, max = 2000, message = "La descripción debe tener entre 20 y 2000 caracteres")
    private String descripcion;

    //TODO: Agregar relacion con cultivos, imagenes

    @Valid
    @NotEmpty(message = "Debe configurar al menos la tarifa base")
    private List<DTOTarifa> tarifas;

    private List<@Size(min = 5, max = 200, message = "El ítem debe tener entre 5 y 200 caracteres") String> incluye;

    private List<@Size(min = 5, max = 200, message = "El ítem debe tener entre 5 y 200 caracteres") String> noIncluye;

    @Valid
    private List<DTOFaq> faqs;

    @NotNull(message = "El estado de la actividad es requerido")
    private String estado;
}
