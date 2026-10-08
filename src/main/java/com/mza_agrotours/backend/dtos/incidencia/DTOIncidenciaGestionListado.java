package com.mza_agrotours.backend.dtos.incidencia;

import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTOIncidenciaGestionListado {
    private UUID id;
    private String titulo;
    private String nombreUsuario;
    private String descripcionCorta; // texto truncado
    private LocalDateTime fechaHoraIncio;
    private LocalDateTime fechaHoraFin;
    private EstadoIncidenciaNombre estado;
}
