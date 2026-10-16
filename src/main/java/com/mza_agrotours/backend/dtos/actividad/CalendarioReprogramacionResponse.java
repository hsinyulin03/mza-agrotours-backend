package com.mza_agrotours.backend.dtos.actividad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CalendarioReprogramacionResponse {
    private LocalDateTime fechaHoraPagoReservaMasAntigua;
    private DTOCalendarioGestionDiasResponse calendarioGestionDiasResponse;
}
