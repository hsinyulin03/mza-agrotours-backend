package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DuracionNombre {
    RAPIDA("Rápida"),
    MEDIA("Media"),
    LARGA("Larga");

    private final String nombre;

}