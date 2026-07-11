package com.mza_agrotours.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioGetDTO {
    private String nombre;
    private String email;
    private String telefono;
    private String identificacion;
    private String tipoIdentificacion;
}
