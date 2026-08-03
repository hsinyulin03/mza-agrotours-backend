package com.mza_agrotours.backend.dtos.roles_permisos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RolGetShortDTO {
    private String id;
    private String nombre;
    private String descripcion;
}
