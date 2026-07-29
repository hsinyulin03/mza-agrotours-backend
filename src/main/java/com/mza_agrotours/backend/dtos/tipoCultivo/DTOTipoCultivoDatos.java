package com.mza_agrotours.backend.dtos.tipoCultivo;

import com.mza_agrotours.backend.enums.EstacionalidadNombre;
import lombok.Data;

import java.util.List;


@Data
public class DTOTipoCultivoDatos {
    private String nombre;
    private String descripcion;
    private List<String> beneficios;
    private List<EstacionalidadNombre> estacionalidadPorMes;
}