package com.mza_agrotours.backend.dtos.tipoCultivo;

import com.mza_agrotours.backend.enums.EstacionalidadNombre;
import lombok.Data;

@Data
public class DTOEstacionalidad {
    private EstacionalidadNombre nombre;
    private String colorMuestra;
}
