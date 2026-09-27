package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DTOConfiguracionDiasResponse {
    private LocalDate fechaMaxima;
    private int ventanaMaximaDias;

    // Tarifas vigentes por rango etario (recordatorio para el productor)
    private List<RangoEtarioReservaDTO> tarifas;
}
