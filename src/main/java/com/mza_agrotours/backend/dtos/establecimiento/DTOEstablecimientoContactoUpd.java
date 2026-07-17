package com.mza_agrotours.backend.dtos.establecimiento;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DTOEstablecimientoContactoUpd {
    // Contacto
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,16}$", message = "El teléfono debe tener un formato válido y entre 7 y 16 caracteres")
    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100, message = "El email debe tener como máximo 100 caracteres")
    private String email;
}
