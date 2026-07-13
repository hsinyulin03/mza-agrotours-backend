package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.entities.actividad.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public abstract class ActividadMapper {
    // US-ACT-02
    public abstract DTOActividadDetalle actividadToDetalleDto(Actividad actividad);

    //US-ACT-06
    public abstract DTOActividades actividadToActividadesDto(Actividad actividad);

    //US-ACT-07
    public abstract DTOCalendarioActividadDia actividadToDTOCalendarioActividadDia(Actividad actividad);

    //US-ACT-12
    public abstract DTOListadoActividadVisitante actividadToDTOListadoActividadVisitante(Actividad actividad);

    @AfterMapping
    public void llenarListasComplejas(Actividad actividad, @MappingTarget DTOActividadDetalle dto) {
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
    public void llenarDatosTarjetaActividades(Actividad actividad, @MappingTarget DTOActividades dto) {
        java.time.LocalDate ahora = java.time.LocalDate.now();

        //Obtener el Precio Regular
        dto.setPrecioRegular(obtenerPrecioBaseVigente(actividad));

        // Armar los textos de Días y Horas Disponibles ("LUNES 09:00 - 13:00")
        List<String> diasDisponibles = obtenerDiasYHorasDisponibles(actividad);
        dto.setDiasYHorasDisponibles(diasDisponibles);
    }


    @AfterMapping
    public void llenarDatosCalendarioDetalle(Actividad actividad, @MappingTarget DTOCalendarioActividadDia dto) {
        dto.setDiasYHorasDisponibles(obtenerDiasYHorasDisponibles(actividad));
    }


    @AfterMapping
    public void llenarDatosTarjetaVisitante(Actividad actividad, @MappingTarget DTOListadoActividadVisitante dto) {
        dto.setPrecioDesde(obtenerPrecioBaseVigente(actividad));
    }

    //Métodos auxiliares
    private BigDecimal obtenerPrecioBaseVigente(Actividad actividad) {

        LocalDate hoy = LocalDate.now();

        if (actividad.getActividadRangoEtarios() == null) {
            return BigDecimal.ZERO;
        }

        return actividad.getActividadRangoEtarios().stream()
                .filter(tarifa -> tarifa.getRangoEtario() != null && tarifa.getRangoEtario().isEsTarifaBase())
                .filter(r -> (r.getFechaValidaDesde() == null || !r.getFechaValidaDesde().isAfter(hoy)) &&
                        (r.getFechaValidaHasta() == null || !r.getFechaValidaHasta().isBefore(hoy)))
                .findFirst()
                .map(ActividadRangoEtario::getPrecio)
                .orElse(BigDecimal.ZERO);
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
                //Formateamos el output
                .map(this::formatearDiaYHora)
                .toList();
        return diasDisponibles;

    }

    private String capitalizarPrimerLetra(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    private String formatearDiaYHora(ActividadLogAltasDia dia) {
        String horaInicio = dia.getHoraInicio().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        String horaFin = dia.getHoraFin().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        String nombreDia = capitalizarPrimerLetra(dia.getDia().name());

        return nombreDia + " " + horaInicio + " - " + horaFin;
    }

}