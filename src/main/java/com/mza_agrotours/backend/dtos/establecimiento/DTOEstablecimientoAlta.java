package com.mza_agrotours.backend.dtos.establecimiento;

import com.mza_agrotours.backend.validation.NumeroTelefono;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DTOEstablecimientoAlta {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(min = 1, max = 100, message = "La razón social debe tener entre 1 y 100 caracteres")
    private String razonSocial;

    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El CUIT debe contener únicamente números y tener exactamente 11 dígitos")
    private String cuit;

   @NotBlank
   @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;

    // Ubicación / Domicilio legal
    @NotBlank(message = "El domicilio legal es obligatorio")
    @Size(min = 1, max = 200, message = "El domicilio legal debe tener entre 1 y 200 caracteres")
    private String ubicacion;

    @NotNull(message = "Debe seleccionarse un departamento")
    private UUID departamentoId;

    // Contacto
    @NotBlank(message = "El teléfono es obligatorio")
    @NumeroTelefono
    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100, message = "El email debe tener como máximo 100 caracteres")
    private String email;

    // Operación
    @NotBlank(message = "El CVU es obligatorio")
    @Pattern(regexp = "\\d{22}", message = "El CVU debe contener únicamente números y tener exactamente 22 dígitos")
    private String cvu;
    private List<UUID> cultivos;
}
