package com.mza_agrotours.backend.dtos.roles_permisos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class RolCreateRequest {
    @NotNull
    @Size(min = 3, max = 40)
    private String nombre;

    @NotNull
    @Size(max = 100)
    private String descripcion;

    @NotNull
    @Size(min = 1)
    private List<String> permisos;
}
