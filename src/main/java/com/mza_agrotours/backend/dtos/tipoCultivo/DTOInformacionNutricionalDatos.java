package com.mza_agrotours.backend.dtos.tipoCultivo;

import com.mza_agrotours.backend.enums.UnidadNutricional;
import lombok.Data;

@Data
public class DTOInformacionNutricionalDatos {
    private String nombre;
    private String valor;
    private UnidadNutricional unidad;
}