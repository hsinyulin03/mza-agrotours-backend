package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.util.UUID;

@Data
public class DTOEstablecimientoCard {
    private UUID id;
    private String nombre;
    private String departamento;
    private String descripcion;
    private String estado;
}
