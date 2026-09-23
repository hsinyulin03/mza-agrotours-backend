package com.mza_agrotours.backend.dtos.solicitud_establecimiento;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudEstablecimientoPruebaDTO {
    private UUID id;

    private String key;
    private String nombre;
    private String extension;
}
