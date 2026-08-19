package com.mza_agrotours.backend.entities.roles_permisos;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.enums.PermisoCodigo;
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
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PermisoCodigo codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @ManyToOne(optional = false)
    private TipoPermiso tipoPermiso;
}
