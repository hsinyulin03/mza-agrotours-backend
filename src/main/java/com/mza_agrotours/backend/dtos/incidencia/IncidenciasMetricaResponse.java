package com.mza_agrotours.backend.dtos.incidencia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciasMetricaResponse {
    private long totalAbiertas;    // Reportadas + en revisión
    private long totalTodas;
    private Map<String, Long> conteosPorEstado;
}
