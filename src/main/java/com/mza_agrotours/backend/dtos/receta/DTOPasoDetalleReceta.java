package com.mza_agrotours.backend.dtos.receta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOPasoDetalleReceta {
    private Integer numeroPaso;
    private String descripcion;
}
