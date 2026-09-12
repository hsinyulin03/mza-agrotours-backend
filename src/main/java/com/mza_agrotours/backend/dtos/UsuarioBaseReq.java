package com.mza_agrotours.backend.dtos;

import com.mza_agrotours.backend.validation.EdadMaxima;
import jakarta.validation.constraints.*;
import lombok.*;
import com.mza_agrotours.backend.validation.NumeroTelefono;
import com.mza_agrotours.backend.validation.SinCaracteresEspeciales;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioBaseReq {
    @NotNull
    @Size(min = 3, max = 20, message = "El nombre debe tener entre 3 y 20 caracteres")
    @Size(min = 3, max = 20)
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
    @NumeroTelefono(estricto = true,
            message = "El teléfono debe estar en formato internacional con '+' y código de país, ej: +5492611234567")
    private String telefono;

    @NotNull
    @Size(min = 1, max = 20)
    private String identificacion;

    @NotNull
    private String tipoIdentificacion;
}
