package com.mza_agrotours.backend.entities.actividad;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Actividad extends BaseEntity {
    private String nombre;

    @OneToMany
    @JoinColumn(name = "actividad_id")
    private List<ActividadDia> actividadDias;
}
