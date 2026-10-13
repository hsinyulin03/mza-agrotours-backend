package com.mza_agrotours.backend.dtos.PanelProductor;

import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaProximaDTO {
    private UUID reservaId;
    private UUID actividadId;
    private UUID actividadDiaId;
    private LocalDateTime fechaHoraInicio;
    private String actividadNombre;
    private String visitanteNombre;
    private Integer personas;
    private EstadoReservaNombre estado;
}
