package com.mza_agrotours.backend.dtos.establecimiento;

import lombok.Data;

import java.util.UUID;

@Data
public class DTOCultivoEstablecimientoResponse {
    private UUID id;
    private String nombre;
}
