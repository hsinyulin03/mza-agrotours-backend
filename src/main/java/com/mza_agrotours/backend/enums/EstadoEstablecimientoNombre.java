package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EstadoEstablecimientoNombre {
    ACTIVO("Activo"),
    SUSPENDIDO("Suspendido"),

    // Se añade este estado principalmente para desviar
    // las queries que checkeen el estado de un establecimiento
    // si no se cambia durante la baja, entonces queda en ACTIVO
    // el estado debe dictar la query, la fecha lo menos posible
    DADO_DE_BAJA("Dado de baja");

    private final String nombre;

}
