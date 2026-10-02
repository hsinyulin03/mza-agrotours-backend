package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.enums.Dia;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//vigencia completa de un log de altas (sin recortar al mes) y sus días agrupados por horario
@Data
@AllArgsConstructor
public class DTOConfiguracionVigencia {
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private List<DTOConfiguracionHorario> horarios = new ArrayList<>();

    public DTOConfiguracionVigencia(LocalDate fechaDesde, LocalDate fechaHasta) {
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    //agrega el día a su horario; si el horario no existe lo crea (ordenados por hora de inicio)
    public void agregar(Dia dia, LocalTime horaInicio, LocalTime horaFin) {
        DTOConfiguracionHorario horario = horarios.stream()
                .filter(h -> h.getHoraInicio().equals(horaInicio) && h.getHoraFin().equals(horaFin))
                .findFirst()
                .orElse(null);
        if (horario == null) {
            horario = new DTOConfiguracionHorario(horaInicio, horaFin);
            horarios.add(horario);
            horarios.sort(Comparator.comparing(DTOConfiguracionHorario::getHoraInicio));
        }
        horario.agregar(dia);
    }
}
