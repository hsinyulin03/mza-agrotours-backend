package com.mza_agrotours.backend.entities.roles_permisos;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Permiso extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private PermisoNombre nombre;

    @Column(nullable = false)
    private String descripcion;

    @ManyToOne(optional = false)
    private TipoPermiso tipoPermiso;
}
