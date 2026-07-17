package com.mza_agrotours.backend.dtos.establecimiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DTOEstablecimientoDescripcionUpd {
    @NotBlank
    @Size(max = 2000)
    private String descripcion;
}
