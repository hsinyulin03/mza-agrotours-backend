package com.mza_agrotours.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "actividad_inclusiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ActividadInclusiones extends BaseEntity {
    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false)
    private Boolean incluye; // true = Qué incluye, false = Qué NO incluye

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id")
    @JsonIgnore
    private Actividad actividad;
}
