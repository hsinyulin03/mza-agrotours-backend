package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EstadoEstacionalidad {
    COSECHA("Cosecha"),
    CRECIMIENTO("Crecimiento"),
    REPOSO("Reposo");

    private final String nombre;

}