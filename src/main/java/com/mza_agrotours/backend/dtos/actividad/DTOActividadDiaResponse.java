package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class DTOActividadDiaResponse {
        private UUID id;
        private LocalDate fecha;
        private String estadoActual;

        // Métricas para la barra de progreso fraccionada
        private int cuposMaximos;    // Ej: 10

        //TODO- falta agregar estas métricas
       /* private int cuposPagados;    // Ej: 6
        private int cuposPendientes; // Ej: 2*/

}
