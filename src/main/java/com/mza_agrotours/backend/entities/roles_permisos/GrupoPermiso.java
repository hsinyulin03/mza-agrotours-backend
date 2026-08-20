package com.mza_agrotours.backend.entities.roles_permisos;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GrupoPermiso extends BaseEntity {
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String icono;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_permiso_id")
    private TipoPermiso tipoPermiso;

    @ManyToMany
    @JoinTable(
            name="grupo_permiso_permiso",
            joinColumns = @JoinColumn(name = "grupo_permiso_id")
            , inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    private List<Permiso> permisos;
}
