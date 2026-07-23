package com.mza_agrotours.backend.dtos.establecimiento;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class DTODetalleEstablecimientoActividad {
    private UUID id;

    private String nombre;

    private List<String> cultivos;

    private BigDecimal precioDesde;

    private Double puntuacion;
}
