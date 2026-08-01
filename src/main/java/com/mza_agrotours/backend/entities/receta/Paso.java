package com.mza_agrotours.backend.entities.receta;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Paso extends BaseEntity {
    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false, length = 200)
    private String descripcion;
}
