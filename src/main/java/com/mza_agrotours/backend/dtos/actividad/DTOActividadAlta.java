package com.mza_agrotours.backend.dtos.actividad;


import com.mza_agrotours.backend.entities.actividad.EstadoActividad;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;


import java.time.LocalDate;
import java.util.List;

@Data

//US-ACT-03 AltaActividad
public class DTOActividadAlta {
    // PASO 1: Información general
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 5, max = 80, message = "El nombre debe tener entre 5 y 80 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9 áéíóúÁÉÍÓÚñÑ]+$", message = "No se aceptan caracteres especiales")
    private String nombre;

    @NotBlank(message = "La descripción es requerida")
    @Size(min = 20, max = 2000, message = "La descripción debe tener entre 20 y 2000 caracteres")
    private String descripcion;

    @NotNull(message = "El estado de la actividad es requerido")
    private String estado; // Enum: BORRADOR o PUBLICADO

    //  PASO 2: Detalles de la experiencia
    private List<@Size(min = 5, max = 200, message = "El ítem debe tener entre 5 y 200 caracteres") String> incluye;

    private List<@Size(min = 5, max = 200, message = "El ítem debe tener entre 5 y 200 caracteres") String> noIncluye;

    @Valid
    private List<DTOFaq> faqs;

    //PASO 3: Participantes y tarifas
    @NotNull(message = "Este campo es obligatorio")
    @Min(value = 1, message = "El cupo máximo debe ser mayor a 0")
    private int cuposMax;

    @Valid
    @NotEmpty(message = "Debe configurar al menos la tarifa base")
    private List<DTOTarifa> tarifas;

    // PASO 4: Disponibilidad
    @NotNull(message = "La fecha de inicio de vigencia es requerida")
    @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado")
    private LocalDate fechaDesde;

    @NotNull(message = "La fecha de fin de vigencia es requerida")
    private LocalDate fechaHasta;

    @Valid
    @NotEmpty(message = "Debe configurar al menos un día de la semana para la actividad")
    private List<DTODiaDisponibilidad> diasDisponibles;

}
