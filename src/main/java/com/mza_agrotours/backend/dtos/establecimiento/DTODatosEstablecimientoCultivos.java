package com.mza_agrotours.backend.dtos.establecimiento;

import lombok.Data;

@Data
public class DTODatosEstablecimientoCultivos {
    private String id;
    private String nombre;
    // indica si el establecimiento tiene actividades activas para ese cultivo
    // true: el cultivo no se muestra para eliminar
    private boolean tieneActividadesActivas;
}
