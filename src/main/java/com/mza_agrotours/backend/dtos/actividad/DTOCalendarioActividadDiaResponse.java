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

    // Para armar el Calendario (Solo los días del mes solicitado)
    private List<DTOActividadDiaResponse> diasDelMes;

    //TODO- Falta agregar las metricas globales y barras sobre reservas
}
