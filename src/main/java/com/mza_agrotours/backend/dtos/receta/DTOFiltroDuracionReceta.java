package com.mza_agrotours.backend.dtos.receta;

import com.mza_agrotours.backend.enums.DuracionNombre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOFiltroDuracionReceta {
    private DuracionNombre duracion;
    private Long cantidadRecetas;
}
