package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.receta.DTORecetaDetalleM;
import com.mza_agrotours.backend.dtos.receta.DTORecetaListado;
import com.mza_agrotours.backend.entities.receta.Receta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RectaMapper {
    @Mapping(target = "cultivos", ignore = true)
    @Mapping(target = "ingredientes", ignore = true)
    @Mapping(target = "pasos", ignore = true)
    DTORecetaDetalleM recetaToDtoDetalle(Receta receta);
    @Mapping(target = "nombresCultivos", ignore = true)
    @Mapping(source = "duracion.nombre.nombre", target = "duracionNombre")
    @Mapping(target = "cantidadPasos", ignore = true)
    DTORecetaListado recetaToDtoListado(Receta receta);
}
