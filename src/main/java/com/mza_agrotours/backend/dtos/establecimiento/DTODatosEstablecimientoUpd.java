package com.mza_agrotours.backend.dtos.establecimiento;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DTODatosEstablecimientoUpd {
    //Identidad
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;
    // Contacto
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    // Operación
    @NotBlank(message = "El CVU es obligatorio")
    @Pattern(regexp = "\\d{22}", message = "El CVU debe contener únicamente números y tener exactamente 22 dígitos")
    private String cvu;
    // Cultivos
    private List<UUID> cultivosIds;
}
