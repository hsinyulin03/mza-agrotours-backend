package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DTOActividadAltaResponse {
    UUID idActividad;
    String mensaje;
    List<String> advertencias;
}
