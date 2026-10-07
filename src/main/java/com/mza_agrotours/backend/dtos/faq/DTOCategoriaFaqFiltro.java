package com.mza_agrotours.backend.dtos.faq;

import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTOCategoriaFaqFiltro {
    private CategoriaFAQNombre nombre;
    private Long cantidadFaqs;
}
