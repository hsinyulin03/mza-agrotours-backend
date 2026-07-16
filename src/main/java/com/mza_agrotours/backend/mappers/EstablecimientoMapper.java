package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.establecimiento.DTOEstablecimientoAlta;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EstablecimientoMapper {
    Establecimiento dtoEstablecimientoAltaToEstablecimiento(DTOEstablecimientoAlta dto);
}
