package com.mza_agrotours.backend.dtos.estadisticasproductor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodoDTO {
    String valor;        // "30d" | "6m" | "12m"
    String label;       // "Últimos 30 días"
    LocalDate desde;
    LocalDate hasta;
    LocalDate desdeAnterior;
    LocalDate hastaAnterior;
}
