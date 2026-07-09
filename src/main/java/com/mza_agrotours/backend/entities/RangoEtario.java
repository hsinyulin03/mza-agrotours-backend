package com.mza_agrotours.backend.entities;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rango_etario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RangoEtario extends BaseEntity {

        @Column(nullable = false, length = 40)
        private String nombre;

        @Column(name = "edad_minima", nullable = false)
        private Integer edadMinima;

        @Column(name = "edad_maxima", nullable = false)
        private Integer edadMaxima;

        @Column(name = "fecha_hora_baja")
        private LocalDateTime fechaHoraBaja;
}

