package com.mza_agrotours.backend.dtos.incidencia;

import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaCreateResponse {
    private UUID id;
    String mensaje;
}
