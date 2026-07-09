package com.mza_agrotours.backend.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mza_agrotours.backend.enums.Dia;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "actividad_log_altas_dia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


public class ActividadLogAltasDia extends BaseEntity{
    @Enumerated(EnumType.STRING)
    private Dia dia;

    private LocalTime horaInicio;
    private LocalTime horaFin;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_log_altas_id")
    @JsonIgnore
    private ActividadLogAltas actividadLogAltas;
}
