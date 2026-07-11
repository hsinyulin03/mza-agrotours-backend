package com.mza_agrotours.backend.entities.establecimiento;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Establecimiento extends BaseEntity {
    private String razonSocial;
    private String ubicacion;

    @OneToMany(mappedBy = "establecimiento")
    private List<Actividad> actividades;
}
