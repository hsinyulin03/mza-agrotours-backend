package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Mes {
    ENERO("Enero"),
    FEBRERO("Febrero"),
    MARZO("Marzo"),
    ABRIL("Abril"),
    MAYO("Mayo"),
    JUNIO("Junio"),
    JULIO("Julio"),
    AGOSTO("Agosto"),
    SEPTIEMBRE("Septiembre"),
    OCTUBRE("Octubre"),
    NOVIEMBRE("Noviembre"),
    DICIEMBRE("Diciembre");

    private final String nombre;

}