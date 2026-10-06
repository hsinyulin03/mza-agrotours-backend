package com.mza_agrotours.backend.dtos.estadisticasproductor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerieDTO {
    String title;
    String sub;
    List<BarraDTO> bars;
}
