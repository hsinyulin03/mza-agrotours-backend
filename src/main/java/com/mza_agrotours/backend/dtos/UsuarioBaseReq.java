package com.mza_agrotours.backend.dtos;

import com.mza_agrotours.backend.validation.EdadMaxima;
import com.mza_agrotours.backend.validation.NumeroTelefono;
import com.mza_agrotours.backend.validation.SinCaracteresEspeciales;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioBaseReq {
    @NotNull
    @Size(min = 3, max = 40, message = "El nombre debe tener entre 3 y 20 caracteres")
    @SinCaracteresEspeciales
    private String nombre;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual")
    @EdadMaxima(120)
    private LocalDate fechaNacimiento;

    @NotNull
    private String paisIso2;

    @NotBlank(message = "El teléfono es obligatorio")
    @NumeroTelefono
    private String telefono;

    @NotNull
    @Size(min = 5, max = 20)
    private String identificacion;

    @NotNull
    private String tipoIdentificacion;
}
