package com.mza_agrotours.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "actividad_faq")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ActividadFAQ extends BaseEntity{
    @Column(nullable = false, length = 200)
    private String pregunta;

    @Column(nullable = false, length = 200)
    private String respuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id")
    @JsonIgnore
    private Actividad actividad;
}
