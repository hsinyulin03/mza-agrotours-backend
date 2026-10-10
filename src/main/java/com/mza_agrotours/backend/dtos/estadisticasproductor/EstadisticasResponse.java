package com.mza_agrotours.backend.dtos.estadisticasproductor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasResponse {
    PeriodoDTO periodo;
    KpisDTO kpis;
    SerieDTO serie;
    List<ActividadPerformanceDTO> actividades;
}
