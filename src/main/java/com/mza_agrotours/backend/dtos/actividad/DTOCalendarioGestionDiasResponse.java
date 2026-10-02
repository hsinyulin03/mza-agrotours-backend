package com.mza_agrotours.backend.dtos.actividad;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    // Vigencia general: desde más temprano y hasta más tardío entre todos los logs de altas
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;

    // Encabezado: un elemento por log de altas que cruza el mes, con sus horarios
    private List<DTOConfiguracionVigencia> configuraciones;

    // Para armar el Calendario (Solo los días del mes solicitado)
    private List<DTOActividadDiaResponse> diasDelMes;
}
