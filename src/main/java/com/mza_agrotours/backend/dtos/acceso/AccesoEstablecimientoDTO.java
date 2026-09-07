package com.mza_agrotours.backend.dtos.acceso;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccesoEstablecimientoDTO {
    private String id;
    private String nombre;
    private String estado;
}
