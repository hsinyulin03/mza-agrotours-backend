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

    public void cambiarEstado(EstadoActividadDia estado, LocalDateTime ahora, String motivo) {

        // Al último estado le damos FechaHoraFin en caso de que exista
        if (this.estadoActual != null){
            this.estadoActual.setFechaHoraFin(ahora);
        }

        ActividadDiaEstado nuevoADE = new ActividadDiaEstado(ahora, null, motivo, estado);

        this.estadoActual = nuevoADE;
        this.estados.add(nuevoADE);
    }

}
