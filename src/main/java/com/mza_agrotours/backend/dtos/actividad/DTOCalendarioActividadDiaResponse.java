package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.util.List;
import java.util.UUID;

//US-ACT-07: Consultar todos los días disponibles para una actividad
@Data
public class DTOCalendarioActividadDiaResponse {
    private UUID id;
    private String nombre;
    private String estado;
    private List<String> diasYHorasDisponibles;
    private List<DTOCultivoResponse> cultivos;
    private String nombreEstablecimiento;
    private String nombreDepartamento;
    private DTOMetricasReservasGlobales metricas;

    // Para armar el Calendario (Solo los días del mes solicitado)
    private List<DTOActividadDiaResponse> diasDelMes;
}
