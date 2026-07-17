package com.mza_agrotours.backend.entities.actividad;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.RangoEtario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ActividadRangoEtario extends BaseEntity {
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "fecha_valida_desde")
    private LocalDate fechaValidaDesde;

    @Column(name = "fecha_valida_hasta")
    private LocalDate fechaValidaHasta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rango_etario_id", nullable = false)
    private RangoEtario rangoEtario;

    //Para indicar cuál es el rango que se mostrará en precio base
    private boolean esTarifaBase;

}



