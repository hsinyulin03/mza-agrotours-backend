package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.incidencia.DTOListadoIncidenciaVisitanteResponse;
import com.mza_agrotours.backend.entities.incidencia.Incidencia;
import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import org.mapstruct.Mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface IncidenciaMapper {

    default DTOListadoIncidenciaVisitanteResponse toDTO(Incidencia incidencia) {
        if (incidencia == null) return null;

        EstadoIncidenciaNombre estadoNombre = incidencia.getEstadoActual() != null
                ? incidencia.getEstadoActual().getEstado().getNombre()
                : null;

        String motivo = null;
        if (estadoNombre == EstadoIncidenciaNombre.RESUELTA || estadoNombre == EstadoIncidenciaNombre.DESESTIMADA) {
            motivo = incidencia.getEstadoActual().getMotivo();
        }

        Integer diasTranscurridos = null;
        if (incidencia.getFechaHoraIncio() != null) {
            diasTranscurridos = (int) ChronoUnit.DAYS.between(
                    incidencia.getFechaHoraIncio().toLocalDate(),
                    LocalDate.now()
            );
        }

        return DTOListadoIncidenciaVisitanteResponse.builder()
                .id(incidencia.getId())
                .titulo(incidencia.getTitulo())
                .descripcion(incidencia.getDescripcion())
                .estado(estadoNombre)
                .fechaHoraIncio(incidencia.getFechaHoraIncio())
                .diasTranscurridos(diasTranscurridos)
                .motivo(motivo)
                .build();
    }

    default List<DTOListadoIncidenciaVisitanteResponse> toDTOList(List<Incidencia> incidencias) {
        if (incidencias == null) return Collections.emptyList();
        return incidencias.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
