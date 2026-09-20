package com.mza_agrotours.backend.dtos.actividad;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DTOConteoReservasPorActividad {
    private UUID actividadId;
    private Long cantidad;
}
