package com.mza_agrotours.backend.dtos.rangoEtario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTORangoEtarioGet {
    private UUID id;
    private String nombre;
    private Integer edadMinima;
    private Integer edadMaxima;
}
