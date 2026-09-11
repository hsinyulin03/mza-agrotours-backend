package com.mza_agrotours.backend.dtos.establecimiento;

import com.mza_agrotours.backend.validation.NumeroTelefono;
import com.mza_agrotours.backend.validation.SinCaracteresEspeciales;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DTOUpdEstablecimientoRequest {
    //Identidad
    @NotNull
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    @SinCaracteresEspeciales
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;
    // Contacto
    @NotBlank(message = "El teléfono es obligatorio")
    @NumeroTelefono
    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    // Operación
    @NotBlank(message = "El CVU es obligatorio")
    @Pattern(regexp = "\\d{22}", message = "El CVU debe contener únicamente números y tener exactamente 22 dígitos")
    private String cvu;

}
