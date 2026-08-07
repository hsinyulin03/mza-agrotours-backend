package com.mza_agrotours.backend.dtos.administrador_sistemas;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdminSistemasCreateReq {
    @NotNull
    @Email
    @NotBlank
    private String emailUsuario;

    @NotNull
    private UUID rolId;
}
