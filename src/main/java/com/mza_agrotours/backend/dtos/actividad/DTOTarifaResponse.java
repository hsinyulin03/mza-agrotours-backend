package com.mza_agrotours.backend.dtos.actividad;


import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DTOTarifaResponse {
    private UUID id;
    private String nombre;
    private Integer edadMinima;
    private Integer edadMaxima;
    private BigDecimal precio;
    private boolean esTarifaBase;
}
