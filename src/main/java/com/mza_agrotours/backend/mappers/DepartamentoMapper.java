package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.DepartamentoDTO;
import com.mza_agrotours.backend.entities.Departamento;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartamentoMapper {
    DepartamentoDTO departamentoToDepartamentoDTO(Departamento departamento);
    List<DepartamentoDTO> departamentoListToDepartamentoDTOList(List<Departamento> departamentos);
}
