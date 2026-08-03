package com.mza_agrotours.backend.dtos.administrador_sistemas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdminSistemasGetDTO {
    private String id;
    private String nombreUsuario;
    private String emailUsuario;
    private String identificacion;
    private String nombreRol;
    private Boolean esLider;
}
