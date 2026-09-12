package com.mza_agrotours.backend.dtos.establecimiento;

import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
public class DTOCatalogoEstablecimientoVisitante {
    private UUID id;
    private String nombre;
    private String razonSocial;
    private String descripcion;
    private DTODptoEstablecimientoResponse dptoEstablecimiento;
    private List<DTOCultivoEstablecimientoResponse> cultivos;
    private Integer cantidadActividades;
}
