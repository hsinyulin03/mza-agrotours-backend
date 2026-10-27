package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;
import java.util.UUID;

@ParameterObject
public record FiltroActividadRequest(String busqueda,
                                     List<UUID> cultivosIds,
                                     UUID departamentoId,
                                     @Min(0) @Max(5) Integer calificacionMin,
                                     @Positive Double precioDesde,
                                     @Positive Double precioHasta,
                                     Boolean ascByCalificacion,
                                     Boolean ascByPrecio) {
}
