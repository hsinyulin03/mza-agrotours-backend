package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data

public class DTOListadoReservasResumenResponse {
    private UUID id;
    private String nombre;
    private String estadoDia;
    private List<DTOCultivoResponse> cultivos;
    private String nombreEstablecimiento;
    private String nombreDepartamento;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private BigDecimal ingresoEstimadoDelDia;
    private long cantidadTotalReservas;
}
