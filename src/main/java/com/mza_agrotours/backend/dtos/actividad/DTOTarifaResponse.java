package com.mza_agrotours.backend.dtos.actividad;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class DTOTarifaResponse {
    private String nombre;
    private Integer edadMinima;
    private Integer edadMaxima;
    private BigDecimal precio;
}
