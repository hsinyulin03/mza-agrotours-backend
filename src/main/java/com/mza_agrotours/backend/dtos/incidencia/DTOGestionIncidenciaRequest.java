package com.mza_agrotours.backend.dtos.incidencia;

import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTOGestionIncidenciaRequest {
    @NotNull
    private EstadoIncidenciaNombre estado;

    @Size(max = 2000, message = "El motivo debe como máximo 2000 caracteres")
    private String motivo;
}
