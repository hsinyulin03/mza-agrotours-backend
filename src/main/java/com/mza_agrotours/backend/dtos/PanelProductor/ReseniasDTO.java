package com.mza_agrotours.backend.dtos.PanelProductor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReseniasDTO {
    private Double promedio;
    private long total;
    private List<ReseniaRecienteDTO> ultimas;
}
