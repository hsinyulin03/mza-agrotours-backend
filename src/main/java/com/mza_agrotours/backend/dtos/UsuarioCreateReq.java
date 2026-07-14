package com.mza_agrotours.backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateReq {
    @NotNull
    @Size(min = 3, max = 20)
    private String nombre;

    @NotNull
    @Email
    private String email;

    @NotNull
    private LocalDate fechaNacimiento;

    @NotNull
    private String paisIso2;

    @NotNull
    @Size(min = 7, max = 15)
    private String telefono;

    @NotNull
    @Size(min = 1, max = 20)
    private String identificacion;

    @NotNull
    private String tipoIdentificacion;

    @NotNull
    @Size(min = 8)
    private String password;
}
