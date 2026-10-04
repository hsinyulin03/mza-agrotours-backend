package com.mza_agrotours.backend.dtos.incidencia;

import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTOGestionIncidenciaRequest {
    private EstadoIncidenciaNombre estado;
    private String motivo;
}
