package com.mza_agrotours.backend.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString

public enum CategoriaFAQNombre {

    GENERAL("General"),
    RESERVAS("Reservas"),
    CUENTA("Cuenta"),
    PRODUCTORES("Productores"),
    PAGOS("Pagos");

    @JsonValue
    private final String nombre;
}
