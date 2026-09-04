package com.mza_agrotours.backend.dtos.administrador_sistemas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class EstablecimientoSuspenderReq {
    @NotBlank
    @Size(max = 200)
    private String motivo;
}
