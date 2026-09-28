package com.mza_agrotours.backend.dtos.actividad;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DTOResumenRangoEtario {
        private String rango;     // Ej: "Adulto"
        private Long cantidad;    // Ej: 2
}
