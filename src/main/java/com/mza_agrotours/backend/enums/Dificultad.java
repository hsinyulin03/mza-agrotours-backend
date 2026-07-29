package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Dificultad {
    FACIL("Fácil"),
    MEDIA("Media"),
    DIFICIL("Difícil");

    private final String nombre;

}