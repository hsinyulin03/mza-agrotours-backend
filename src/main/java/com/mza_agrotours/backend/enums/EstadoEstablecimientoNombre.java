package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EstadoEstablecimientoNombre {
    ACTIVO("Activo"),
    SUSPENDIDO("Suspendido");

    private final String nombre;

}
