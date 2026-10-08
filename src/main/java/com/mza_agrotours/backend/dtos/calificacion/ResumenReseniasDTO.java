package com.mza_agrotours.backend.dtos.calificacion;

import lombok.Data;

import java.util.List;

@Data
public class ResumenReseniasDTO {
    private Double promedio;
    private long totalResenias;
    private List<DistribucionPuntajeDTO> distribucion;
}
