package com.mza_agrotours.backend.dtos.clima;

import com.mza_agrotours.backend.enums.CondicionClima;

import java.io.Serializable;
import java.util.List;

public record ClimaPronosticoGetResponse(String departamento, List<ClimaDia> clima) implements Serializable {
    public record ClimaDia(String fecha, Double temperaturaMed, Double temperaturaMin, Double temperaturaMax, Double probabilidadLluvia, CondicionClima condicion) implements Serializable {}
}
