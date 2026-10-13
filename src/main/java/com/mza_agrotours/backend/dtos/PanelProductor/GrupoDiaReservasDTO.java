package com.mza_agrotours.backend.dtos.PanelProductor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoDiaReservasDTO {
    private LocalDate fecha;
    private List<ReservaProximaDTO> reservas;
}
