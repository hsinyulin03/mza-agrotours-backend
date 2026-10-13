package com.mza_agrotours.backend.dtos.PanelProductor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReseniaRecienteDTO {
    private int puntaje;
    private String resenia;
    private LocalDateTime fecha;
    private String visitanteNombre;
    private String actividadNombre;
}
