package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class DTOActividadFotoReq {
    @NotBlank
    @Size(max = 255)
    private String key;

    @Size(max = 255)
    private String nombre;
}
