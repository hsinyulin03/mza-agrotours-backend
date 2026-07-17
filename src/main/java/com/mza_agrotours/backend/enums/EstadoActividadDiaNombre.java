package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public enum EstadoActividadDiaNombre {
    ACTIVA("Activa"),
    CANCELADA("Cancelada"),
    FINALIZADA("Finalizada"),
    REPROGRAMADA("Reprogramada");

    private final String nombre;

}
