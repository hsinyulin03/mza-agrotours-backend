package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RolProtegido {
    ADMIN_LIDER("Administrador Líder"),
    PRODUCTOR_LIDER("Productor Líder");

    private final String nombre;
}
