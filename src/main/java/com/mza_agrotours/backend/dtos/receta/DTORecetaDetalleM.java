package com.mza_agrotours.backend.dtos.receta;

import com.mza_agrotours.backend.enums.Dificultad;
import com.mza_agrotours.backend.enums.DuracionNombre;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DTORecetaDetalleM {
    private UUID id;
    private String nombre;
    private List<DTORecetaDetalleCultivoM> cultivos;
    private Dificultad dificultad;
    private Integer tiempoMinsAprox;
    private Integer porciones;
    private String descripcion;
    private List<String> ingredientes;
    private List<String> pasos;
}
