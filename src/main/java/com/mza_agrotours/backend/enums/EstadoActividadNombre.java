package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EstadoActividadNombre {
        BORRADOR("Borrador"),
        PUBLICADO("Publicado"),
        DADO_DE_BAJA("Dado de baja");

        private final String nombre;

}
