package com.mza_agrotours.backend.dtos.calificacion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalificacionPromedioParams {
    private Integer cantidadCalificacionesFicticias;
    private Double puntajeCalificacionFicticia;
}
