package com.mza_agrotours.backend.dtos.solicitud_establecimiento;

import com.mza_agrotours.backend.entities.solicitud_establecimiento.EstadoSolicitudEstablecimientoNombre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SolicitudEstablecimientoUserShotDTO {
    private String id;
    private String nombreEstablecimiento;
    private String razonSocial;
    private String domicilioLegal;
    private String cuit;
    private LocalDateTime fechaHoraAlta;
    private EstadoSolicitudEstablecimientoNombre estado;
}
