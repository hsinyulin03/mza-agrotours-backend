package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.enums.Dia;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DTOConfiguracionHorario {
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private List<Dia> dias = new ArrayList<>();

    public DTOConfiguracionHorario(LocalTime horaInicio, LocalTime horaFin) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    //amplía el rango de fechas cuando hay dos log con el mismo horario y agrega el día sin repetir (ordenado de lunes a domingo)
    public void agregar(Dia dia, LocalDate desde, LocalDate hasta) {
        if (fechaDesde == null || desde.isBefore(fechaDesde)) fechaDesde = desde;
        if (fechaHasta == null || hasta.isAfter(fechaHasta)) fechaHasta = hasta;
        if (!dias.contains(dia)) {
            dias.add(dia);
            dias.sort(null);
        }
    }
}
