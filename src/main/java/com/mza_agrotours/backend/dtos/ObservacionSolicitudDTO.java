package com.mza_agrotours.backend.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ObservacionSolicitudDTO {
    @NotNull
    private String observacion;

    @NotNull
    private String estado;
}
