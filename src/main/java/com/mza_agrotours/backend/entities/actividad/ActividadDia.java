package com.mza_agrotours.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ActividadDia extends BaseEntity{
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private LocalDateTime fechaHoraBaja;
    private Integer cuposMax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id")
    @JsonIgnore
    private Actividad actividad;

    @OneToMany(mappedBy = "actividadDia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActividadDiaEstado> estados = new ArrayList<>();

    public void addEstado(ActividadDiaEstado estado) {
        if (this.estados == null) {
            this.estados = new ArrayList<>();
        }
        this.estados.add(estado);
        estado.setActividadDia(this); // Seteamos la FK en la base de datos
    }


}
