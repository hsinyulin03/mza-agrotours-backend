package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

@Data
public class DTOUbicacion {
    private String nombreEstablecimiento;
    private String direccionEstablecimiento;
    //TODO: fix para pasar la lat y lon del establecimiento, ahora muestra lat y lon del departamento
    private Double latitude;
    private Double longitude;
}
