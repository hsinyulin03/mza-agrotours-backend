package com.mza_agrotours.backend.dtos.incidencia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOGestionIncidenciaResponse {
    private UUID id;
    private String mensaje;
}
