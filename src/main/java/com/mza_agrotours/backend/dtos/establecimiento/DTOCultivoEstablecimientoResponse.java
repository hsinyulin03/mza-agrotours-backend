package com.mza_agrotours.backend.dtos.establecimiento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@NoArgsConstructor
@Data
public class DTOCultivoEstablecimientoResponse {
    private UUID id;
    private String nombre;

    public DTOCultivoEstablecimientoResponse(UUID id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}
