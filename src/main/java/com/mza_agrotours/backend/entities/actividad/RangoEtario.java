package com.mza_agrotours.backend.entities.actividad;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;

@Entity
@Getter
public class RangoEtario extends BaseEntity {
    private String nombre;
}
