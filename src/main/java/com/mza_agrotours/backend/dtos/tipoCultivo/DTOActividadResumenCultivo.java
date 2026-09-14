package com.mza_agrotours.backend.dtos.tipoCultivo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DTOActividadResumenCultivo {
    private UUID id;
    private String titulo;
    private String nombreEstablecimiento;
    private String nombreDepartamento;
    private BigDecimal precioRegular;

}
