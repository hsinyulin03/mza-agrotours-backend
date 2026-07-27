package com.mza_agrotours.backend.entities.actividad;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ActividadRangoEtario extends BaseEntity {
    @Column(nullable = false, length = 40)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "edad_minima", nullable = false)
    private Integer edadMinima;

    @Column(name = "edad_maxima", nullable = false)
    private Integer edadMaxima;

    @Column(name = "fecha_hora_baja")
    private LocalDateTime fechaHoraBaja;

    //Para indicar cuál es el rango que se mostrará en precio base
    private boolean esTarifaBase;

}



