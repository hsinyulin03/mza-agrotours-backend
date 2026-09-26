package com.mza_agrotours.backend.dtos.tipoCultivo;

import lombok.Data;

import java.util.UUID;

@Data
public class DTOTipoCultivoVisitante {
    private UUID id;
    private String nombre;
    private String resumenCosecha; // ej: "Mar–Abr"
    private boolean enTemporada;
}
