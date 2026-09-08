package com.mza_agrotours.backend.dtos.tipoCultivo;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DTOTipoCultivoDetalleVisitante {
    private UUID id;
    private String nombre;
    private String descripcion;
    private List<String> beneficios;
    private List<DTOEstacionalidadMes> calendario; // los 12 meses completos, para la barra
    private String porcionReferencia;
    private List<DTOInformacionNutricionalDatos> informacionNutricional;
    private List<DTORecetaResumenCultivo> recetas;
    private List<DTOActividadResumenCultivo> actividades;
}