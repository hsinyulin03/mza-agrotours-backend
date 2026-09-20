package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class DTOActividadDiaResponse {
        private UUID id;
        private LocalDate fecha;
        private String estadoActual;
        private LocalTime horaInicio;
        private LocalTime horaFin;

        // Métricas para la barra de progreso fraccionada
        private int cuposMaximos;    // Ej: 10
        private int cuposPagados;    // Ej: 6
        private int cuposPendientes; // Ej: 2

        public void aplicarCupos(DTOCuposPorDia cupos) {
                if (cupos == null) return;
                this.cuposPendientes = cupos.getCuposPendientes().intValue();
                this.cuposPagados = cupos.getCuposPagados().intValue();
        }

        public int getCuposLibres() {
                return Math.max(0, cuposMaximos - cuposPagados - cuposPendientes);
        }
}
