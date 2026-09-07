package com.mza_agrotours.backend.dtos.establecimiento;

import lombok.Data;

import java.util.UUID;
@Data
public class DTOBajaEstablecimientoResponse {
    private UUID idestablecimiento;
    private String mensaje;
}
