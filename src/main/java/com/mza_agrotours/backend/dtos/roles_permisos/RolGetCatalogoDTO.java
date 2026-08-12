package com.mza_agrotours.backend.dtos.roles_permisos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@NoArgsConstructor
@Getter
@Setter
public class RolGetCatalogoDTO {
    private String id;
    private String nombre;
    private String descripcion;
    private List<String> permisos;
    private Integer cantidadUsuarios;
    private Boolean esProtegido;
}
