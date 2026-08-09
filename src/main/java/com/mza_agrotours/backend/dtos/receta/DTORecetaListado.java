package com.mza_agrotours.backend.dtos.receta;

import com.mza_agrotours.backend.enums.Dificultad;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DTORecetaListado {
    private UUID id;
    private String nombre;
    private List<String> nombresCultivos;
    private Dificultad dificultad;
    private Integer tiempoMinsAprox;
    private String duracionNombre;
    private Integer cantidadPasos;
    private Integer porciones;
}