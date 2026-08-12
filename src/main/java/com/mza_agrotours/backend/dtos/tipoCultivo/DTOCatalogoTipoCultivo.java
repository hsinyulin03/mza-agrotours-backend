package com.mza_agrotours.backend.dtos.tipoCultivo;

import lombok.Data;

import java.util.List;

@Data
public class DTOCatalogoTipoCultivo {
    private Integer totalCultivos;
    private Integer totalRecetas;
    private List<DTOTipoCultivoListado> cultivos;
}
