package com.mza_agrotours.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "actividad_rango_etario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ActividadRangoEtario extends BaseEntity{
    @Column(nullable = false)
    private Float precio;

    @Column(name = "fecha_valida_desde")
    private LocalDateTime fechaValidaDesde;

    @Column(name = "fecha_valida_hasta")
    private LocalDateTime fechaValidaHasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id")
    @JsonIgnore
    private Actividad actividad;

    //TODO-Agregar Rango Etario
    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rango_etario_id")
    private RangoEtario rangoEtario;*/

}



