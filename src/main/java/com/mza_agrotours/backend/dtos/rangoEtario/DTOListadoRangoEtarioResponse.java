package com.mza_agrotours.backend.dtos.rangoEtario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTOListadoRangoEtarioResponse {
    private List<DTORangoEtarioGet> rangosActivos;
    private List<String> alertasHuecos;
}
