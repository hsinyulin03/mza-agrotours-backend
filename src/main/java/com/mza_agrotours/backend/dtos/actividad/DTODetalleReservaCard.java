package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class DTODetalleReservaCard {
    private UUID id;
    private String estadoReserva;
    private Integer cantidadTotalPersona;
    private List<DTOResumenRangoEtario> resumenRangoEtario;
    private List<DTODetalleVisitantesCard> visitantes;
    private BigDecimal montoTotalReserva;
}
