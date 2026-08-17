package com.mza_agrotours.backend.entities.roles_permisos;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Rol extends BaseEntity {
    // TODO: unique index with fechaHoraBaja = NULL
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    private LocalDateTime fechaHoraBaja;

    @Column(nullable = false)
    private Boolean esProtegido;

    @ManyToMany
    @JoinTable(
            name = "rol_permiso",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    private List<Permiso> permisos;

    @ManyToOne(optional = false)
    private TipoPermiso tipoPermiso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id")
    private Establecimiento establecimiento;
}
