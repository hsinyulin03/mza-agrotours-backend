package com.mza_agrotours.backend.entities.cultivo;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.enums.EstacionalidadNombre;
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
public class Estacionalidad extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstacionalidadNombre nombre;

    @Column(nullable = false, length = 20)
    private String colorMuestra;
}

