package com.mza_agrotours.backend.dtos.tipoCultivo;

import com.mza_agrotours.backend.enums.EstacionalidadNombre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class DTOTipoCultivoAM {

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 60, message = "Máximo 60 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción es requerida")
    @Size(max = 500, message = "Máximo 500 caracteres")
    private String descripcion;

    @NotEmpty(message = "Cargá al menos un beneficio")
    private List<@NotBlank(message = "Completá o quitá este beneficio")
    @Size(max = 100, message = "Máximo 100 caracteres") String> beneficios;

    @NotNull(message = "La estacionalidad es requerida")
    @Size(min = 12, max = 12, message = "Debe indicarse la estacionalidad de los 12 meses")
    private List<EstacionalidadNombre> estacionalidadPorMes; // indice 0 = Enero índice 11 = Diciembre
}