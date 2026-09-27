package com.mza_agrotours.backend.dtos.solicitud_establecimiento;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * La url vive lo que dure la firma, asi que se pide cuando el admin va a abrir
 * la prueba y no cuando se arma el detalle de la solicitud.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudEstablecimientoPruebaUrlDTO {
    private String nombre;
    private String url;
}
