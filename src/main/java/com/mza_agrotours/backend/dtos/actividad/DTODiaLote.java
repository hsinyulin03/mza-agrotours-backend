package com.mza_agrotours.backend.dtos.actividad;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DTODiaLote {
    private LocalDate fecha;
    private String diaSemana; // "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"
}
