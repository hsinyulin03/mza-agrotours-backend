package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.establecimiento.*;
import com.mza_agrotours.backend.entities.TipoCultivo;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EstablecimientoMapper {
    Establecimiento dtoEstablecimientoAltaToEstablecimiento(DTOEstablecimientoAlta dto);
    // DTO consultar establecimiento- productor
    @Mapping(source = "departamento.nombre", target = "localidad")
    @Mapping(target = "cultivos", ignore = true)
    DTODatosEstablecimiento establecimientoToDtoDatosEstablecimiento(Establecimiento establecimiento);
    // DTO consultar establecimientoS - visitante
    @Mapping(target = "cultivos", ignore = true)
    @Mapping(target = "cantidadActividades", ignore = true)
    DTOConsultarEstablecimientoSVisitante establecimientoToDtoConsultarEstableciminetoS(Establecimiento establecimiento);
    // DTO consultar detalle de un establecimiento

    @Mapping(source = "departamento.nombre", target = "departamento")
    @Mapping(target = "cultivos", ignore = true)
    @Mapping(target = "actividades", ignore = true)
    DTODetalleEstablecimientoVisitantes establecimientoToDtoDetalleVisitantes(Establecimiento establecimiento);

    @Mapping(target = "cultivos", ignore = true)
    @Mapping(target = "precioDesde", ignore = true)
    @Mapping(target = "puntuacion", ignore = true)
    DTODetalleEstablecimientoActividad actividadToDtoDetalle(Actividad actividad);
}

