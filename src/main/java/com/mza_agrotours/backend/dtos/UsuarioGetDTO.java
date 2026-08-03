package com.mza_agrotours.backend.dtos;

import com.mza_agrotours.backend.entities.roles_permisos.TipoPermisoNombre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioGetDTO {
    private String nombre;
    private String email;
    private String telefono;
    private String paisIso2;
    private String fechaNacimiento;
    private String identificacion;
    private String tipoIdentificacion;
    private List<TipoPermisoNombre> tipoPermisos;
}
