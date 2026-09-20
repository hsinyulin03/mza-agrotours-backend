package com.mza_agrotours.backend.dtos.actividad;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DTOMetricasReservasGlobales {
    private long personasPendientes;
    private long personasPagadas;
    private long personasFinalizadas;
}
