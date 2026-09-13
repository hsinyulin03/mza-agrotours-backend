package com.mza_agrotours.backend.dtos.receta;

import com.mza_agrotours.backend.enums.Dificultad;
import com.mza_agrotours.backend.enums.DuracionNombre;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class DTORecetaCatalogoVisitante {
    private UUID id;
    private String nombre;
    private List<DTOCultivoRecetaResponse> cultivos;
    private Dificultad dificultad;
    private String tiempo; // formateado "1 h 15 min"
    private DuracionNombre duracion;
    private Integer porciones;
    private Integer cantidadPasos;
}
