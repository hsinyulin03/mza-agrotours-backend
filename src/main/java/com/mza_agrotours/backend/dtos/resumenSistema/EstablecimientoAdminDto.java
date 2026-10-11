package com.mza_agrotours.backend.dtos.resumenSistema;

import java.util.UUID;

public class EstablecimientoAdminDto {
    private UUID id;
    private String nombre;

    public EstablecimientoAdminDto(UUID id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}
