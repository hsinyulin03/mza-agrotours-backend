package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.receta.DTODetalleVisitanteReceta;
import com.mza_agrotours.backend.dtos.receta.DTOIngredienteDetalleReceta;
import com.mza_agrotours.backend.dtos.receta.DTOPasoDetalleReceta;
import com.mza_agrotours.backend.dtos.receta.DTORecetaCatalogoVisitante;
import com.mza_agrotours.backend.dtos.receta.DTORecetaDetalleM;
import com.mza_agrotours.backend.dtos.receta.DTORecetaListado;
import com.mza_agrotours.backend.entities.receta.Ingrediente;
import com.mza_agrotours.backend.entities.receta.Paso;
import com.mza_agrotours.backend.entities.receta.Receta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecetaMapper {
    @Mapping(target = "cultivos", ignore = true)
    @Mapping(target = "ingredientes", ignore = true)
    @Mapping(target = "pasos", ignore = true)
    DTORecetaDetalleM recetaToDtoDetalle(Receta receta);

    @Mapping(target = "nombresCultivos", ignore = true)
    @Mapping(source = "duracion.nombre.nombre", target = "duracionNombre")
    @Mapping(target = "cantidadPasos", ignore = true)
    DTORecetaListado recetaToDtoListado(Receta receta);

    @Mapping(target = "cultivos", ignore = true)
    @Mapping(target = "cantidadPasos", ignore = true)
    @Mapping(source = "duracion.nombre", target = "duracion")
    @Mapping(target = "tiempo", expression = "java(formatearTiempo(receta.getTiempoMinsAprox()))")
    DTORecetaCatalogoVisitante recetaToDtoCatalogoVisitante(Receta receta);

    @Mapping(target = "cultivos", ignore = true)
    @Mapping(source = "duracion.nombre", target = "duracion")
    @Mapping(target = "tiempo", expression = "java(formatearTiempo(receta.getTiempoMinsAprox()))")
    DTODetalleVisitanteReceta recetaToDtoDetalleVisitante(Receta receta);

    @Mapping(source = "numero", target = "numeroPaso")
    DTOPasoDetalleReceta pasoToDto(Paso paso);

    DTOIngredienteDetalleReceta ingredienteToDto(Ingrediente ingrediente);

    default String formatearTiempo(Integer minutos) {
        if (minutos == null) {
            return null;
        }
        int horas = minutos / 60;
        int minutosRestantes = minutos % 60;

        if (horas == 0) {
            return minutosRestantes + " min";
        }
        if (minutosRestantes == 0) {
            return horas + " h";
        }
        return horas + " h " + minutosRestantes + " min";
    }
}
