package com.mza_agrotours.backend.dtos.incidencia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTOIncidenciaFiltro {
    private String estado;
    private String busqueda;
}
