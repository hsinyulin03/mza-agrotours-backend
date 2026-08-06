package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.dtos.reservas.RangoEtarioReservaDTO;
import com.mza_agrotours.backend.entities.actividad.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ActividadMapper {
    // US-ACT-02
    @Mapping(target = "incluye", ignore = true)
    @Mapping(target = "noIncluye", ignore = true)
    @Mapping(target = "preguntasFrecuentes", ignore = true)
    @Mapping(target = "tarifas", ignore = true)
    @Mapping(target = "precioRegular", ignore = true)
    @Mapping(target = "cultivos", ignore = true)
    DTOActividadDetalleResponse actividadToDTOActividadDetalle(Actividad actividad);

    //US-ACT-06
    @Mapping(target = "estado", source = "estado.nombre")
    @Mapping(target = "diasYHorasDisponibles", ignore = true)
    @Mapping(target = "precioRegular", ignore = true)
    @Mapping(target = "cultivos", ignore = true)
    DTOActividadesResponse actividadToDTOActividades(Actividad actividad);

    //US-ACT-07
    @Mapping(target = "estado", source = "estado.nombre")
    @Mapping(target = "diasYHorasDisponibles", ignore = true)
    @Mapping(target = "diasDelMes", ignore = true)
    @Mapping(target = "cultivos", ignore = true)
    DTOCalendarioActividadDiaResponse actividadToDTOCalendarioActividadDia(Actividad actividad);

    @Mapping(source = "cuposMax", target = "cuposMaximos")
    @Mapping(source = "estadoActual.estado.nombre", target = "estadoActual")
    @Mapping(target = "fecha", expression = "java(dia.getFechaHoraInicio() != null ? dia.getFechaHoraInicio().toLocalDate() : null)")
    DTOActividadDiaResponse actividadDiatoDTOActividadDia(ActividadDia dia);

    //US-ACT-12
    @Mapping(target = "precioRegular", ignore = true)
    @Mapping(target = "cultivos", ignore = true)
    DTOListadoActividadVisitanteResponse actividadToDTOListadoActividadVisitante(Actividad actividad);

    //US-ACT-04
    @Mapping(target = "estado", source = "estado.nombre")
    @Mapping(target = "incluye", ignore = true)
    @Mapping(target = "noIncluye", ignore = true)
    @Mapping(target = "faqs", ignore = true)
    @Mapping(target = "rangosEtarios", ignore = true)
    @Mapping(target = "cultivos", ignore = true)
    DTOActividadGetResponse actividadToDTOActividadGetResponse(Actividad actividad);

    //US-RESE-01
    RangoEtarioReservaDTO actividadRangoEtarioToDTO(ActividadRangoEtario actividadRangoEtarios);

    @AfterMapping
    default void llenarListasComplejas(Actividad actividad, @MappingTarget DTOActividadDetalleResponse dto) {
        List<String> incluye = obtenerInclusiones(actividad.getInclusiones(), true);
        List<String> noIncluye = obtenerInclusiones(actividad.getInclusiones(), false);
        List<DTOFaqResponse> faqs = obtenerFaqs(actividad.getFaqs());
        List<DTOTarifaResponse> tarifas = obtenerTarifas(actividad.getActividadRangoEtarios());
        BigDecimal precioBase = obtenerPrecioBaseVigente(actividad);
        List <DTOCultivoResponse> cultivosAsociados= obtenerCultivosAsociados(actividad);

        dto.setIncluye(incluye);
        dto.setNoIncluye(noIncluye);
        dto.setPreguntasFrecuentes(faqs);
        dto.setTarifas(tarifas);
        dto.setPrecioRegular(precioBase);
        dto.setCultivos(cultivosAsociados);

    }


    @AfterMapping
    default void llenarDatosTarjetaActividades(Actividad actividad, @MappingTarget DTOActividadesResponse dto) {

        //Obtener el Precio Regular
        BigDecimal precioBase = obtenerPrecioBaseVigente(actividad);
        dto.setPrecioRegular(precioBase);

        dto.setEstado(obtenerNombreEstado(actividad.getEstado()));

        // Armar los textos de Días y Horas Disponibles ("LUNES 09:00 - 13:00")
        List<String> diasDisponibles = obtenerDiasYHorasDisponibles(actividad);
        dto.setDiasYHorasDisponibles(diasDisponibles);

        List <DTOCultivoResponse> cultivosAsociados= obtenerCultivosAsociados(actividad);
        dto.setCultivos(cultivosAsociados);
    }


    @AfterMapping
    default void llenarDatosCalendarioDetalle(Actividad actividad, @MappingTarget DTOCalendarioActividadDiaResponse dto) {
        List<String> diasDisponibles = obtenerDiasYHorasDisponibles(actividad);
        dto.setDiasYHorasDisponibles(diasDisponibles);

        dto.setEstado(obtenerNombreEstado(actividad.getEstado()));

        List <DTOCultivoResponse> cultivosAsociados= obtenerCultivosAsociados(actividad);
        dto.setCultivos(cultivosAsociados);
    }

    @AfterMapping
    default void llenarDatosTarjetaVisitante(Actividad actividad, @MappingTarget DTOListadoActividadVisitanteResponse dto) {
        BigDecimal precioBase = obtenerPrecioBaseVigente(actividad);
        dto.setPrecioRegular(precioBase);
        List <DTOCultivoResponse> cultivosAsociados= obtenerCultivosAsociados(actividad);
        dto.setCultivos(cultivosAsociados);
    }
    @AfterMapping
    default void llenarListasGetResponse(Actividad actividad, @MappingTarget DTOActividadGetResponse dto) {
        List<String> incluye = obtenerInclusiones(actividad.getInclusiones(), true);
        List<String> noIncluye = obtenerInclusiones(actividad.getInclusiones(), false);
        List<DTOFaqResponse> faqs = obtenerFaqs(actividad.getFaqs());
        List<DTOTarifaResponse> tarifas = obtenerTarifas(actividad.getActividadRangoEtarios());
        List <DTOCultivoResponse> cultivosAsociados= obtenerCultivosAsociados(actividad);

        dto.setIncluye(incluye);
        dto.setNoIncluye(noIncluye);
        dto.setFaqs(faqs);
        dto.setRangosEtarios(tarifas);
        dto.setCultivos(cultivosAsociados);
    }

    //Métodos auxiliares
    private BigDecimal obtenerPrecioBaseVigente(Actividad actividad) {

        if (actividad.getActividadRangoEtarios() == null) {
            return null;
        }

        return actividad.getActividadRangoEtarios().stream()
                .filter(ActividadRangoEtario::isEsTarifaBase)
                .filter(r -> (r.getFechaHoraBaja() == null))
                .map(ActividadRangoEtario::getPrecio)
                .findFirst()
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

    private List<String> obtenerInclusiones(List<ActividadInclusiones> inclusiones, boolean incluye) {
        if (inclusiones == null) {
            return List.of();
        }
        return inclusiones.stream()
                .filter(inc -> inc.getIncluye() == incluye)
                .map(ActividadInclusiones::getDescripcion)
                .toList();
    }
    private List<DTOFaqResponse> obtenerFaqs(List<ActividadFAQ> faqs) {
        if (faqs == null) {
            return List.of(); // Devuelve lista vacía en lugar de null para evitar errores futuros
        }

        return faqs.stream()
                .map(faq -> {
                    DTOFaqResponse f = new DTOFaqResponse();
                    f.setPregunta(faq.getPregunta());
                    f.setRespuesta(faq.getRespuesta());
                    return f;
                })
                .toList();
    }

    private List<DTOTarifaResponse> obtenerTarifas(List<ActividadRangoEtario> rangosEtarios) {
        if (rangosEtarios == null) {
            return List.of();
        }

        return rangosEtarios.stream()
                .filter(r -> r.getFechaHoraBaja() == null)
                .map(tarifa -> {
                    DTOTarifaResponse t = new DTOTarifaResponse();
                    t.setId(tarifa.getId());
                    t.setNombre(tarifa.getNombre());
                    t.setEdadMinima(tarifa.getEdadMinima());
                    t.setEdadMaxima(tarifa.getEdadMaxima());
                    t.setPrecio(tarifa.getPrecio());
                    t.setEsTarifaBase(tarifa.isEsTarifaBase());
                    return t;
                })
                .toList();
    }
    private String obtenerNombreEstado(EstadoActividad estado) {
        if (estado != null && estado.getNombre() != null) {
            return estado.getNombre().name();
        }
        return null;
    }

    private List<DTOCultivoResponse> obtenerCultivosAsociados(Actividad actividad){
        List<DTOCultivoResponse> cultivosAsociados = actividad.getCultivos().stream()
                .map(c -> new DTOCultivoResponse(c.getId(), c.getNombre()))
                .collect(Collectors.toList());
        return cultivosAsociados;
    }

}