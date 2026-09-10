package com.mza_agrotours.backend.dtos.receta;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DTOFiltroCultivoReceta {
    private UUID id;
    private String nombre;
    private Long cantidadRecetas;
}
