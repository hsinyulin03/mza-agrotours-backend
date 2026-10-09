package com.mza_agrotours.backend.dtos.faq;

import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOListadoAdminFaq {
    private UUID id;
    private CategoriaFAQNombre categoria;
    private String pregunta;
    private String respuesta;
}
