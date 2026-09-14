package com.mza_agrotours.backend.dtos.receta;

import com.mza_agrotours.backend.enums.Dificultad;
import com.mza_agrotours.backend.enums.DuracionNombre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTODetalleVisitanteReceta {
    private UUID id;
    private String nombre;
    private String descripcion;
    private String tiempo; // formateado, ej: "1 h 15 min"
    private DuracionNombre duracion;
    private Integer porciones;
    private Dificultad dificultad;
    private List<DTOCultivoRecetaResponse> cultivos;
    private List<DTOIngredienteDetalleReceta> ingredientes;
    private List<DTOPasoDetalleReceta> pasos;
}
