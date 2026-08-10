package com.mza_agrotours.backend.dtos.tipoCultivo;

import com.mza_agrotours.backend.enums.EstacionalidadNombre;
import com.mza_agrotours.backend.enums.Mes;
import lombok.Data;

@Data
public class DTOEstacionalidadMes {
    private Mes mes;
    private EstacionalidadNombre nombre;
}
