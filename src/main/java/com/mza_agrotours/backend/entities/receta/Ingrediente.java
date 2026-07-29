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
public class Ingrediente extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String nombreycantidad;
}
