package com.mza_agrotours.backend.entities.actividad;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "actividad_rango_etario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ActividadRangoEtario extends BaseEntity {
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "fecha_valida_desde")
    private LocalDateTime fechaValidaDesde;

    @Column(name = "fecha_valida_hasta")
    private LocalDateTime fechaValidaHasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false)
    @JsonIgnore
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rango_etario_id", nullable = false)
    private RangoEtario rangoEtario;

}



