package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class DTOCalendarioGestionDiasResponse {
    private UUID id;
    private String nombre;
    private String nombreEstablecimiento;
    private BigDecimal precioBase;
    private int cupoBase;
    private String estadoActividad;

    // Encabezado: logs de altas del mes agrupados por horario
    private List<DTOConfiguracionHorario> configuraciones;

    // Para armar el Calendario (Solo los días del mes solicitado)
    private List<DTOActividadDiaResponse> diasDelMes;
}
