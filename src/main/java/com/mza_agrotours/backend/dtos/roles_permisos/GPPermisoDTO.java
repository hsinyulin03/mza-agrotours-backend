package com.mza_agrotours.backend.dtos.roles_permisos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GPPermisoDTO {
    private String nombre;
    private String descripcion;
    private String codigo;
}
