package com.mza_agrotours.backend.entities.actividad;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.enums.Dia;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


public class ActividadLogAltasDia extends BaseEntity {

    private LocalTime horaInicio;
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    private Dia dia;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "log_altas_dia_id")
    private List<ActividadDia> actividadesDias = new ArrayList<>();

    public void addActividadDia(ActividadDia dia) {
        this.actividadesDias.add(dia);
    }
}
