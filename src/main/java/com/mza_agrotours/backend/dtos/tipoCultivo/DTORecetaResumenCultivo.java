package com.mza_agrotours.backend.dtos.tipoCultivo;

import com.mza_agrotours.backend.enums.Dificultad;
import lombok.Data;

import java.util.UUID;

@Data
public class DTORecetaResumenCultivo {
    private UUID id;
    private String nombre;
    private String tiempo; // formateado, ej: "1 h 15 min"
    private Integer porciones;
    private Dificultad dificultad;
}