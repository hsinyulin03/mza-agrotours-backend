package com.mza_agrotours.backend.entities.receta;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.enums.DuracionNombre;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Duracion extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DuracionNombre nombre;
    @Column(name = "min_desde", nullable = false)
    private Integer minDesde;
    private Integer minHasta;
    private LocalDateTime fechaHoraBaja;
}
