package com.mza_agrotours.backend.dtos.establecimiento;

import lombok.Data;

import java.util.UUID;
@Data
public class DTOUpdEstablecimientoResponse {
    private String mensaje;
    private DTODatosEstablecimiento datosEstablecimiento;
}
