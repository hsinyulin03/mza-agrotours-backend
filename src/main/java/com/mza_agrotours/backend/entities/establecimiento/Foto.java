package com.mza_agrotours.backend.entities.establecimiento;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Foto extends BaseEntity {
    private String url;
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    private Actividad actividad;
}
