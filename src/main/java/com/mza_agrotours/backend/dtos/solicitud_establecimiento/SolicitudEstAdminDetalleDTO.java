package com.mza_agrotours.backend.dtos.solicitud_establecimiento;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class SolicitudEstAdminDetalleDTO {
    private String id;
    private String nombreEstablecimiento;
    private String razonSocial;
    private String domicilioLegal;
    private String cuit;
    private String telefono;
    private String cvu;
    private String email;
    private String departamento;
    private LocalDateTime fechaHoraAlta;
    private String estado;
    private List<SolicitudEstablecimientoEstadoDTO> estados;
    private List<SolicitudEstablecimientoPruebaDTO> pruebas;

    private String nombreSolicitante;
    private String identificacionSolicitante;
    private String emailSolicitante;
}
