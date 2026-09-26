package com.mza_agrotours.backend.dtos.actividad;


import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadRequest;
import com.mza_agrotours.backend.entities.actividad.EstadoActividad;
import com.mza_agrotours.backend.validation.SinCaracteresEspeciales;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data

//US-ACT-03 AltaActividad
public class DTOActividadAlta {
    // PASO 1: Información general
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 5, max = 80, message = "El nombre debe tener entre 5 y 80 caracteres")
    @SinCaracteresEspeciales
    private String nombre;

    @NotBlank(message = "La descripción es requerida")
    @Size(min = 20, max = 2000, message = "La descripción debe tener entre 20 y 2000 caracteres")
    private String descripcion;

    @NotEmpty(message = "El tipo de cultivo es requerido")
    private List<UUID> cultivos;

    @NotNull(message = "El estado de la actividad es requerido")
    private String estado; // Enum: BORRADOR o PUBLICADO

    @Valid @Size(max = 10)
    private List<ArchivoUploadRequest> fotos;

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
