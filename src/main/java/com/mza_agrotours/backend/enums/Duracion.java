package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Duracion {
    RAPIDA("Rápida"),
    MEDIA("Media"),
    LARGA("Larga");

    private final String nombre;

}