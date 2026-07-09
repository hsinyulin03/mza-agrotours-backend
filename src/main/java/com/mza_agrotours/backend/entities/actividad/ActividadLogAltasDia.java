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
@Table(name = "actividad_log_altas_dia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


public class ActividadLogAltasDia extends BaseEntity {

    private LocalTime horaInicio;
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    private Dia dia;

    @OneToMany(mappedBy = "logAltasDia", cascade = CascadeType.ALL)
    private List<ActividadDia> actividadesDias = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_log_altas_id")
    @JsonIgnore
    private ActividadLogAltas actividadLogAltas;


    public void addActividadDia(ActividadDia dia) {
        actividadesDias.add(dia);
        dia.setLogAltasDia(this); // Relación bidireccional
    }
}
