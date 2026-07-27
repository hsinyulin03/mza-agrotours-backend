package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.solicitud_establecimiento.SolicitudEstablecimientoCreateReq;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.SolicitudEstablecimiento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudEstablecimientoMapper {
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "estados", ignore = true)
    @Mapping(target = "estadoActual", ignore = true)
    @Mapping(target = "departamento", ignore = true)
    SolicitudEstablecimiento solicitudEstablecimientoDtoToSolicitudEstablecimiento(SolicitudEstablecimientoCreateReq solicitudEstablecimientoCreateReq);
}
