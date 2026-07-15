package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.PaisGetDTO;
import com.mza_agrotours.backend.entities.cuenta.Pais;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaisMapper {
    PaisGetDTO paisToPaisGetDTO(Pais pais);
    List<PaisGetDTO> paisListToPaisGetDTOList(List<Pais> paises);
}
