package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UnidadNutricional {
    KCAL("kcal"),
    GRAMOS("g"),
    MILIGRAMOS("mg"),
    MICROGRAMOS("mcg"),
    PORCENTAJE("%");

    private final String nombre;
}
