package com.mza_agrotours.backend.dtos;

import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DTOEstablecimientoAlta {
    private String razonSocial;
    private Long cuit;
    private String descripcion;

    // Ubicación
    private String ubicacion;
    private Long departamentoId;

    // Contacto
    private String telefono;

    @Email(message = "El email no tiene un formato válido")
    private String email;

    // Operación
    private String cvu;
//    private Long productorTitularId;
    private List<Long> cultivos;
}
