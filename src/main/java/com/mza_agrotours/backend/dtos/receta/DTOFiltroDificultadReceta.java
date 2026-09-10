package com.mza_agrotours.backend.dtos.receta;

import com.mza_agrotours.backend.enums.Dificultad;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DTOFiltroDificultadReceta {
    private Dificultad dificultad;
    private Long cantidadRecetas;
}
