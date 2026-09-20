package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DTOCambioEstadoActividadResponse {
    private UUID actividadId;
    private EstadoActividadNombre estadoAnterior;
    private EstadoActividadNombre estadoNuevo;
}
