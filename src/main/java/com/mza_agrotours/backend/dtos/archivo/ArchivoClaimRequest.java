package com.mza_agrotours.backend.dtos.archivo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un archivo que el cliente ya subio al bucket y quiere asociar a una entidad.
 * La key la genero el servidor al firmar; el nombre es el original del usuario
 * y solo se usa para mostrarlo.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ArchivoClaimRequest {
    @NotBlank
    @Size(max = 255)
    private String key;

    @NotBlank
    @Size(max = 255)
    private String nombre;
}
