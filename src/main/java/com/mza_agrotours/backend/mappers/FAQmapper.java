package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.faq.DTOListadoAdminFaq;
import com.mza_agrotours.backend.entities.faq.FAQ;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FAQmapper {
    @Mapping(source = "categoriaFaq.nombre", target = "categoria")
    DTOListadoAdminFaq FAQtoDTOListadoAdminFaq(FAQ faq);
}
