package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.tipoCultivo.DTOEstacionalidad;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOTipoCultivoEditarDetalle;
import com.mza_agrotours.backend.entities.cultivo.Estacionalidad;
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
}
