package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.incidencia.DTOListadoIncidenciaVisitante;
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

    default DTOListadoIncidenciaVisitante toDTO(Incidencia incidencia) {
        if (incidencia == null) return null;

        EstadoIncidenciaNombre estadoNombre = incidencia.getEstadoActual() != null
                ? incidencia.getEstadoActual().getEstado().getNombre()
                : null;

        String motivo = null;
        if (estadoNombre == EstadoIncidenciaNombre.RESUELTA || estadoNombre == EstadoIncidenciaNombre.DESESTIMADA) {
            motivo = incidencia.getEstadoActual().getMotivo();
        }

        Integer diasTranscurridos = null;
        if (incidencia.getFechaHoraInicio() != null) {
            diasTranscurridos = (int) ChronoUnit.DAYS.between(
                    incidencia.getFechaHoraInicio().toLocalDate(),
                    LocalDate.now()
            );
        }

        return DTOListadoIncidenciaVisitante.builder()
                .id(incidencia.getId())
                .titulo(incidencia.getTitulo())
                .descripcion(incidencia.getDescripcion())
                .estado(estadoNombre)
                .fechaHoraInicio(incidencia.getFechaHoraInicio())
                .diasTranscurridos(diasTranscurridos)
                .respuestaAdmin(motivo)
                .build();
    }

    default List<DTOListadoIncidenciaVisitante> toDTOList(List<Incidencia> incidencias) {
        if (incidencias == null) return Collections.emptyList();
        return incidencias.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
