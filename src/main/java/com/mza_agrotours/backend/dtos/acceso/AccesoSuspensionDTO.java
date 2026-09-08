package com.mza_agrotours.backend.dtos.acceso;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class AccesoSuspensionDTO {
    private LocalDateTime fechaHoraFin;
    private String motivo;
}
