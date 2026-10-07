package com.mza_agrotours.backend.dtos.calificacion;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DistribucionPuntajeDTO {
    private Integer puntaje;
    private Long cantidad;
    private Integer porcentaje;


    public DistribucionPuntajeDTO(Integer puntaje, Long cantidad) {
        this.puntaje = puntaje;
        this.cantidad = cantidad;
    }
}
