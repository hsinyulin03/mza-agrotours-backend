package com.mza_agrotours.backend.dtos.actividad;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class DTOPrevisualizacionLoteResponse {
    private int cantidadDiasACrear;
    private int cantidadDiasOcupados;
    private List<DTODiaLote> diasACrear;
    private List<DTODiaLote> diasOcupados;
}
