package com.mza_agrotours.backend.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ObservacionSolicitudDTO {
    @NotNull
    @Size(max = 1000)
    private String observacion;

    @NotNull
    private String estado;
}
