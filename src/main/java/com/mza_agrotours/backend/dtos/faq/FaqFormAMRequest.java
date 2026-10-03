package com.mza_agrotours.backend.dtos.faq;

import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaqFormAMRequest {
    private UUID id;
    private String pregunta;
    private String respuesta;
    private CategoriaFAQNombre categoria;
    private List<CategoriaFAQNombre> categorias;
}
