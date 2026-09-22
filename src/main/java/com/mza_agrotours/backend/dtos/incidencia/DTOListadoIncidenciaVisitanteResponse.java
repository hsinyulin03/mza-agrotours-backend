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
public class DTOListadoIncidenciaVisitanteResponse {
    private UUID id;
    private String titulo;
    private String descripcion;
    private EstadoIncidenciaNombre estado;
    private LocalDateTime fechaHoraIncio;
    private Integer diasTranscurridos; //dias transcurridos desde la fechaHoraIncio hasta la fecha actual
    private String motivo; //solo si es desestimada o resuelta
}
