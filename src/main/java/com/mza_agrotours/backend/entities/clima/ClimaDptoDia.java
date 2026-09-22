package com.mza_agrotours.backend.entities.clima;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.enums.CondicionClima;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"departamento_id", "fecha"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClimaDptoDia extends BaseEntity {
    @ManyToOne(optional = false)
    private Departamento departamento;

    @Column(nullable = false)
    private Double temperaturaMax;

    @Column(nullable = false)
    private Double temperaturaMed;

    @Column(nullable = false)
    private Double temperaturaMin;

    @Column(nullable = false)
    private Double probabilidadLluvia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CondicionClima condicion;

    @Column(nullable = false)
    private LocalDate fecha;
}
