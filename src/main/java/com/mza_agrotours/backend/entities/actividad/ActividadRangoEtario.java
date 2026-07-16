package com.mza_agrotours.backend.entities.actividad;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ActividadRangoEtario extends BaseEntity {
    @ManyToOne
    private RangoEtario rangoEtario;
}
