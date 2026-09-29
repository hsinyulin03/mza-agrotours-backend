package com.mza_agrotours.backend.dtos.clima;

import com.mza_agrotours.backend.enums.CondicionClima;

import java.time.LocalDate;

public record PronosticoSlot(
        LocalDate fecha,
        Double temperatura,
        Double temperaturaMax,
        Double temperaturaMin,
        Double probabilidadLluvia,
        CondicionClima condicion) {
}