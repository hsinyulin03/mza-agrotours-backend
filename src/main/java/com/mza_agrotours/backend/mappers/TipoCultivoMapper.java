package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.tipoCultivo.*;
import com.mza_agrotours.backend.entities.cultivo.Estacionalidad;
import com.mza_agrotours.backend.entities.cultivo.InformacionNutricional;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TipoCultivoMapper {

    // TipoCultivo -> DTOTipoCultivoEditarDetalle (alta / detalle para editar)
    @Mapping(target = "estacionalidadPorMes", ignore = true)
    DTOTipoCultivoEditarDetalle tipoCultivoToDtoEditarDetalle(TipoCultivo tipoCultivo);

    // Estacionalidad -> DTOEstacionalidad (catálogo de opciones con color)
    DTOEstacionalidad estacionalidadToDto(Estacionalidad estacionalidad);

    List<DTOEstacionalidad> estacionalidadesToDto(List<Estacionalidad> estacionalidades);
    // TipoCultivo -> DTOTipoCultivoListado (catálogo admin)
    @Mapping(target = "calendarioEstacionalidad", ignore = true)
    @Mapping(target = "resumenCosecha", ignore = true)
    @Mapping(target = "cantidadRecetas", ignore = true)
    @Mapping(target = "cantidadActividades", ignore = true)
    @Mapping(target = "puedeEliminarse", ignore = true)
    DTOTipoCultivoListado tipoCultivoToDtoListado(TipoCultivo tipoCultivo);

    List<TipoCultivoShortDTO> tipoCultivoToShortDto(List<TipoCultivo> tipoCultivoListado);

    DTOInformacionNutricionalDatos informacionNutricionalToDto(InformacionNutricional informacionNutricional);

    List<DTOInformacionNutricionalDatos> informacionNutricionalToDto(List<InformacionNutricional> informacionNutricional);
}
