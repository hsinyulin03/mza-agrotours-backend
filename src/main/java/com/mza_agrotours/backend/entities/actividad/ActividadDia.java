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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id")
    @JsonIgnore
    private Actividad actividad;

    @ManyToOne
    @JoinColumn(name = "log_altas_dia_id")
    @JsonIgnore
    private ActividadLogAltasDia logAltasDia;

    //Relación "estados"
    @OneToMany(mappedBy = "actividadDia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActividadDiaEstado> estados = new ArrayList<>();

    //Relación "estadoActual"
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "estado_actual_id", referencedColumnName = "id")
    private ActividadDiaEstado estadoActual;

    public void registrarNuevoEstado(ActividadDiaEstado nuevoEstado) {
        if (this.estados == null) {
            this.estados = new ArrayList<>();
        }

        this.estados.add(nuevoEstado);
        nuevoEstado.setActividadDia(this);
        this.estadoActual = nuevoEstado;
    }


}
