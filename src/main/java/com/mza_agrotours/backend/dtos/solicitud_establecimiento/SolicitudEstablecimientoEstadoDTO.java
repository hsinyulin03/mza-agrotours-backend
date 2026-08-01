package com.mza_agrotours.backend.dtos.solicitud_establecimiento;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudEstablecimientoEstadoDTO {
    private String estado;
    private String fecha;
    private String observaciones;
}
