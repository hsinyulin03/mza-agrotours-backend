package com.mza_agrotours.backend.dtos.tipoCultivo;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DTOTipoCultivoListado {
    private UUID id;
    private String nombre;
    private List<DTOEstacionalidadMes> calendarioEstacionalidad; // los 12 cuadraditos
    private String resumenCosecha; // cuando se cosecha ej: "Mar–Abr"
    private Integer cantidadRecetas;
    private Integer cantidadActividades;
    private boolean puedeEliminarse;
}
