package com.mza_agrotours.backend.dtos.solicitud_establecimiento;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la lista de solicitud de establecimiento
 * solicitada por un administrador de sistemas autorizado.
 */
@NoArgsConstructor
@Getter
@Setter
public class SolicitudEstablecimientoShortDTO {
    private String id;
    private String nombreEstablecimiento;
    private String fechaHoraAlta;
    private String estado;
    private String departamento;
    private String nombreSolicitante;
}
