package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.entities.actividad.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public abstract class ActividadMapper {
    // US-ACT-02
    @Mapping(target = "incluye", ignore = true)
    @Mapping(target = "noIncluye", ignore = true)
    @Mapping(target = "preguntasFrecuentes", ignore = true)
    public abstract DTOActividadDetalleResponse actividadToDTOActividadDetalle(Actividad actividad);

    //US-ACT-06
    @Mapping(target = "estado", source = "estado.nombre")
    @Mapping(target = "diasYHorasDisponibles", ignore = true)
    @Mapping(target = "precioRegular", ignore = true)
    public abstract DTOActividadesResponse actividadToDTOActividades(Actividad actividad);

    //US-ACT-07
    @Mapping(target = "estado", source = "estado.nombre")
    @Mapping(target = "diasYHorasDisponibles", ignore = true)
    @Mapping(target = "diasDelMes", ignore = true)
    public abstract DTOCalendarioActividadDiaResponse actividadToDTOCalendarioActividadDia(Actividad actividad);

    @Mapping(source = "cuposMax", target = "cuposMaximos")
    @Mapping(source = "estadoActual.estado.nombre", target = "estadoActual")
    @Mapping(target = "fecha", expression = "java(dia.getFechaHoraInicio() != null ? dia.getFechaHoraInicio().toLocalDate() : null)")
    public abstract DTOActividadDiaResponse actividadDiatoDTOActividadDia(ActividadDia dia);

    //US-ACT-12
    @Mapping(target = "precioRegular", ignore = true)
    public abstract DTOListadoActividadVisitanteResponse actividadToDTOListadoActividadVisitante(Actividad actividad);

    @AfterMapping
    public void llenarListasComplejas(Actividad actividad, @MappingTarget DTOActividadDetalleResponse dto) {
        List<String> incluye = actividad.getInclusiones().stream()
                .filter(ActividadInclusiones::getIncluye)
                .map(ActividadInclusiones::getDescripcion)
                .toList();
        dto.setIncluye(incluye);

        List<String> noIncluye = actividad.getInclusiones().stream()
                .filter(inc -> !inc.getIncluye())
                .map(ActividadInclusiones::getDescripcion)
                .toList();
        dto.setNoIncluye(noIncluye);

        List<DTOFaqResponse> faqsDto = actividad.getFaqs().stream()
                .map(faq -> {
                    DTOFaqResponse faqDto = new DTOFaqResponse();
                    faqDto.setPregunta(faq.getPregunta());
                    faqDto.setRespuesta(faq.getRespuesta());
                    return faqDto;
                })
                .toList();
        dto.setPreguntasFrecuentes(faqsDto);
    }


    @AfterMapping
    public void llenarDatosTarjetaActividades(Actividad actividad, @MappingTarget DTOActividadesResponse dto) {
        java.time.LocalDate ahora = java.time.LocalDate.now();

        //Obtener el Precio Regular
        dto.setPrecioRegular(obtenerPrecioBaseVigente(actividad));
        if (actividad.getEstado() != null && actividad.getEstado().getNombre() != null) {
            dto.setEstado(actividad.getEstado().getNombre().name());
        }
        // Armar los textos de Días y Horas Disponibles ("LUNES 09:00 - 13:00")
        List<String> diasDisponibles = obtenerDiasYHorasDisponibles(actividad);
        dto.setDiasYHorasDisponibles(diasDisponibles);
    }


    @AfterMapping
    public void llenarDatosCalendarioDetalle(Actividad actividad, @MappingTarget DTOCalendarioActividadDiaResponse dto) {
        dto.setDiasYHorasDisponibles(obtenerDiasYHorasDisponibles(actividad));
        if (actividad.getEstado() != null && actividad.getEstado().getNombre() != null) {
            dto.setEstado(actividad.getEstado().getNombre().name());
        }
    }


    @AfterMapping
    public void llenarDatosTarjetaVisitante(Actividad actividad, @MappingTarget DTOListadoActividadVisitanteResponse dto) {
        dto.setPrecioRegular(obtenerPrecioBaseVigente(actividad));
    }

    //Métodos auxiliares
    private BigDecimal obtenerPrecioBaseVigente(Actividad actividad) {

        LocalDate hoy = LocalDate.now();

        if (actividad.getActividadRangoEtarios() == null) {
            return null;
        }

        return actividad.getActividadRangoEtarios().stream()
                .filter(tarifa -> tarifa.getRangoEtario() != null && tarifa.isEsTarifaBase())
                .filter(r -> (r.getFechaValidaDesde() == null || !r.getFechaValidaDesde().isAfter(hoy)) &&
                        (r.getFechaValidaHasta() == null || !r.getFechaValidaHasta().isBefore(hoy)))
                .findFirst()
                .map(ActividadRangoEtario::getPrecio)
                .orElse(null);
    }

    //Armar los textos de Días y Horas Disponibles ("LUNES 09:00 - 13:00")
    private List<String> obtenerDiasYHorasDisponibles(Actividad actividad) {
        LocalDate hoy = LocalDate.now();

        if (actividad.getLogAltas() == null) {
            return List.of();
        }

        // Buscamos la configuración que esté vigente actualmente
        Optional<ActividadLogAltas> configuracionActual = actividad.getLogAltas().stream()
                    // Filtramos la ventana de inicio: que ya haya empezado (Desde <= hoy)
                    .filter(log -> log.getFechaValidaDesde() == null || !log.getFechaValidaDesde().isAfter(hoy))
                    // Filtramos la ventana de fin: que no esté vencida (Hasta >= hoy)
                    .filter(log -> log.getFechaValidaHasta() == null || !log.getFechaValidaHasta().isBefore(hoy))
                    .findFirst();

        if (configuracionActual.isEmpty() || configuracionActual.get().getDias() == null) {
            return List.of();
        }

        List<String> diasDisponibles = configuracionActual.get().getDias().stream()
                //Ordenamos la lista usando el orden natural del Enum Dia
                .sorted(java.util.Comparator.comparing(ActividadLogAltasDia::getDia))
                .map(logDia -> String.format("%s %s - %s", logDia.getDia().getNombre(), logDia.getHoraInicio(),logDia.getHoraFin()))
                .toList();
        return diasDisponibles;

    }

}