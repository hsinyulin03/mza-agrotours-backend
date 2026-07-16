package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.establecimiento.DTODatosEstablecimiento;
import com.mza_agrotours.backend.dtos.establecimiento.DTODatosEstablecimientoCultivos;
import com.mza_agrotours.backend.dtos.establecimiento.DTOEstablecimientoAlta;
import com.mza_agrotours.backend.entities.TipoCultivo;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EstablecimientoMapper {
    Establecimiento dtoEstablecimientoAltaToEstablecimiento(DTOEstablecimientoAlta dto);

    @Mapping(source = "departamento.nombre", target = "localidad")
    @Mapping(target = "cultivos", ignore = true)
    DTODatosEstablecimiento establecimientoToDtoDatosEstablecimiento(Establecimiento establecimiento);

}
