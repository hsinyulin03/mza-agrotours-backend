package com.mza_agrotours.backend.dtos.administrador_sistemas;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class EstablecimientoSuspenderReq {
    @NotBlank
    private String motivo;
}
