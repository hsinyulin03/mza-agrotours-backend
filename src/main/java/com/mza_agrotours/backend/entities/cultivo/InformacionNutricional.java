package com.mza_agrotours.backend.entities.cultivo;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InformacionNutricional extends BaseEntity {
    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 30)
    private float valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UnidadNutricional unidad;
}