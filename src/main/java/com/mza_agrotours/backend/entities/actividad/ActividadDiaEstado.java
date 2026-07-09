package com.mza_agrotours.backend.entities.actividad;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.enums.EstadoActividadDia;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "actividad_dia_estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDiaEstado extends BaseEntity {

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoActividadDia estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_dia_id")
    @JsonIgnore
    private ActividadDia actividadDia;
}
