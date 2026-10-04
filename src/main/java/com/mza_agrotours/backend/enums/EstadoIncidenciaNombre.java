package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EstadoIncidenciaNombre {
    REPORTADA("Reportada"),
    EN_REVISION("En revisión"),
    RESUELTA("Resuelta"),
    DESESTIMADA("Desestimada");
    private final String nombre;
}

