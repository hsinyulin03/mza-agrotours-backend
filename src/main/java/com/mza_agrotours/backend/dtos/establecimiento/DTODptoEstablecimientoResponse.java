package com.mza_agrotours.backend.dtos.establecimiento;

import lombok.Data;

import java.util.UUID;

@Data
public class DTODptoEstablecimientoResponse {
    private UUID idDepartamento;
    private String nombre;

}
