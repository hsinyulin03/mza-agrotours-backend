package com.mza_agrotours.backend.entities.actividad;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.establecimiento.Foto;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy = "actividad")
    private List<ActividadDia> actividadDias;

    @ManyToOne(fetch = FetchType.LAZY)
    private Establecimiento establecimiento;

    @OneToMany(mappedBy = "actividad")
    private List<Foto> fotos;
}
