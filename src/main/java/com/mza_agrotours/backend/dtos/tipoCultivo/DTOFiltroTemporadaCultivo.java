package com.mza_agrotours.backend.dtos.tipoCultivo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DTOFiltroTemporadaCultivo {
    private long totalTodos;
    private long totalEnTemporada;
    private long totalFueraDeTemporada;
}