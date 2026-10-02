package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.enums.Dia;
import lombok.Data;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DTOConfiguracionHorario {
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private List<Dia> dias = new ArrayList<>();

    public DTOConfiguracionHorario(LocalTime horaInicio, LocalTime horaFin) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    //agrega el día sin repetir (ordenado de lunes a domingo)
    public void agregar(Dia dia) {
        if (!dias.contains(dia)) {
            dias.add(dia);
            dias.sort(null);
        }
    }
}
