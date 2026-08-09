package com.mza_agrotours.backend.dtos;

import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class AccesoDTO {
    // Rol data
    private String rolId;
    private String rolNombre;

    // Permiso data
    private TipoPermisoNombre tipoPermiso;
    private List<String> permisos;

    // Establecimiento data (optional)
    private String establecimientoNombre;
    private String establecimientoId;
}
