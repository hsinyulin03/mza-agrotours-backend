package com.mza_agrotours.backend.dtos;

import com.mza_agrotours.backend.validation.EdadMaxima;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioBaseReq {
    @NotNull
    @Size(min = 3, max = 20, message = "El nombre debe tener entre 3 y 20 caracteres")
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

    @NotNull
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "El teléfono debe estar en formato internacional, ej: +5492611234567")
    private String telefono;

    @NotNull
    @Size(min = 1, max = 20, message = "El número de identificación debe tener entre 1 y 20 caracteres")
    private String identificacion;

    @NotNull
    private String tipoIdentificacion;
}
