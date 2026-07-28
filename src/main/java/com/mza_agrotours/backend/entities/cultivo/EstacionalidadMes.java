package com.mza_agrotours.backend.entities.cultivo;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.enums.Mes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EstacionalidadMes extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mes mes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estacionalidad_id", nullable = false)
    private Estacionalidad estacionalidad;
}
