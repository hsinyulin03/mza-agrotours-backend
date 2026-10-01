package com.mza_agrotours.backend.dtos.incidencia;

import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DTOListadoIncidenciaVisitante {
    private UUID id;
    private String titulo;
    private String descripcion;
    private EstadoIncidenciaNombre estado;
    private LocalDateTime fechaHoraInicio;
    private Integer diasTranscurridos; //dias transcurridos desde la fechaHoraIncio hasta la fecha actual
    private String respuestaAdmin; // motivo incdencia solo si es desestimada o resuelta
}
