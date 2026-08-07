package com.mza_agrotours.backend.dtos.receta;

import lombok.Data;

import java.util.List;
@Data
public class DTOCatalogoReceta {
    private Integer totalRecetas;
    private Integer cultivosConReceta;
    private List<DTORecetaListado> recetas;
}
