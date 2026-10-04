package com.mza_agrotours.backend.dtos.incidencia;

import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOFormGestionarIncidencia {
    private String id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaHoraIncio;
    private LocalDateTime fechaHoraFin;
    private EstadoIncidenciaNombre estadoactual;
    private String motivo;
    private List<EstadoIncidenciaNombre> estadosPosibles;
    private List<EstadoIncidenciaNombre> todosLosEstados;
}
