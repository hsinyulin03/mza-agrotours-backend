package com.mza_agrotours.backend.entities.actividad;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actividad_dia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDia extends BaseEntity {

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private LocalDateTime fechaHoraBaja;
    private int cuposMax;


    //Relación "estados"
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "actividad_dia_id")
    private List<ActividadDiaEstado> estados = new ArrayList<>();

    //Relación "estadoActual"
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "estado_actual_id")
    private ActividadDiaEstado estadoActual;

    public void registrarNuevoEstado(ActividadDiaEstado nuevoEstado) {
        this.estados.add(nuevoEstado);
        this.estadoActual = nuevoEstado;
    }

}
