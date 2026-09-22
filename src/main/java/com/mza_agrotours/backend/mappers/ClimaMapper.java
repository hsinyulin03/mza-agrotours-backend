package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.clima.ClimaPronosticoGetResponse;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.clima.ClimaDptoDia;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClimaMapper {
    ClimaPronosticoGetResponse climaAndDepartamentoToDTO(Departamento departamento, List<ClimaDptoDia> clima);

    ClimaPronosticoGetResponse.ClimaDia climaDiaToDTO(ClimaDptoDia climaDia);

    default String departamentoToNombre(Departamento departamento) {
        return departamento.getNombre();
    }
}
