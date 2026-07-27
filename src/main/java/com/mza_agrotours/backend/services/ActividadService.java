package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.entities.actividad.*;
import com.mza_agrotours.backend.enums.Dia;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.exceptions.ResourceNotFoundException;
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.exceptions.actividad.ValidacionMultipleException;
import com.mza_agrotours.backend.mappers.ActividadMapper;
import com.mza_agrotours.backend.repositories.actividad.ActividadRespository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadDiaRepository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ActividadService {

    @Autowired
    private ActividadRespository actividadRepository;

    @Autowired
    private ActividadValidaciones actividadValidaciones;

    @Autowired
    private ActividadMapper actividadMapper;

    @Autowired
    private EstadoActividadRepository estadoActividadRepository;

    @Autowired
    private EstadoActividadDiaRepository estadoActividadDiaRepository;

    //US-ACT-03 Alta de actividad
    @Transactional
    public DTOActividadAltaResponse altaActividad(DTOActividadAlta dto) {

        //Primero hacemos las validaciones del negocio
        List<String> errores = actividadValidaciones.obtenerErroresValidacionActividad(dto);

        if (!errores.isEmpty()) {
            throw new ValidacionMultipleException(errores);
        }

        EstadoActividad estado = obtenerEstado(dto);

        //Paso 1: Información General
        Actividad actividad = new Actividad();
        actividad.setNombre(dto.getNombre());
        actividad.setDescripcion(dto.getDescripcion());
        actividad.setCuposMax(dto.getCuposMax());
        //Guarda el UUID del estado
        actividad.setEstado(estado);

        List<ActividadInclusiones> inclusiones = obtenerInclusiones(dto.getIncluye(), dto.getNoIncluye());
        List<ActividadFAQ> faqs = obtenerFaqs(dto.getFaqs());
        List<ActividadRangoEtario> tarifas = obtenerTarifas(dto.getTarifas());
        ActividadLogAltas logAltas = obtenerLogAltas(dto);
        List<ActividadDia> calendario = generarDiasCalendario(dto, logAltas);

        //setear los valores obtenidos a actividad
        inclusiones.forEach(actividad::addInclusion);
        faqs.forEach(actividad::addFaq);
        tarifas.forEach(actividad::addActividadRangoEtario);
        actividad.addLogAlta(logAltas);
        calendario.forEach(actividad::addActividadDia);

        // Persistir en la base de datos
        Actividad actividadGuardada = actividadRepository.save(actividad);

        List<String> advertencias = calcularHuecos(dto.getTarifas());

        DTOActividadAltaResponse response = new DTOActividadAltaResponse();
        response.setIdActividad(actividadGuardada.getId());
        response.setMensaje("La actividad fue creada exitosamente.");
        response.setAdvertencias(advertencias);

        return response;
    }

    //US-ACT-02:  Consultar detalle de una actividad
    @Transactional(readOnly = true)
    public DTOActividadDetalleResponse obtenerDetallePorId(UUID id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada con ID: " + id));

        if (actividad.getFechaHoraBaja() != null) {
            throw new ValidacionNegocioException("La actividad se encuentra dada de baja");
        }
        return actividadMapper.actividadToDTOActividadDetalle(actividad);
    }

    //US-ACT-06: Listado de actividades de un establecimiento - Vista productor
    @Transactional(readOnly = true)
    public List<DTOActividadesResponse> obtenerListadoActividades(String busqueda, EstadoActividadNombre estado) {
        // TODO- Se debe filtrar por establecimiento
        List<Actividad> actividades = actividadRepository.findByFiltrosDinamicos(busqueda, estado);

        return actividades.stream()
                .map(actividadMapper::actividadToDTOActividades)
                .toList();
    }

    //US-ACT-07: Consultar todos los días disponibles para una actividad
    @Transactional(readOnly = true)
    public DTOCalendarioActividadDiaResponse obtenerDetalleCalendario(UUID actividadId, int mes, int anio){

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        int anioActual = java.time.LocalDate.now().getYear();

        if (anio < anioActual) {
            throw new ValidacionNegocioException("El año no puede ser menor al año actual (" + anioActual + ")");
        }
        // Permite año actual, próximo año y el siguiente (ej: 2026, 2027 y 2028)
        if (anio > anioActual + 2) {
            throw new ValidacionNegocioException("No puedes consultar un calendario con más de 2 años de anticipación");
        }

        DTOCalendarioActividadDiaResponse dto = actividadMapper.actividadToDTOCalendarioActividadDia(actividad);

        List<DTOActividadDiaResponse> diasDelMesDto = actividad.getActividadesDias().stream()
                .filter(dia -> dia.getFechaHoraInicio() != null)
                .filter(dia -> dia.getFechaHoraInicio().getYear() == anio
                        && dia.getFechaHoraInicio().getMonthValue() == mes)
                .map(actividadMapper::actividadDiatoDTOActividadDia)
                .toList();

        dto.setDiasDelMes(diasDelMesDto);

        return dto;
    }

    //US-ACT-12: Listado de actividades de la plataforma - vista del visitante
    @Transactional(readOnly = true)
    public List<DTOListadoActividadVisitanteResponse> explorarActividades() {

        // TODO: Falta implementar filtro por cultivo y por departamento
        // TODO: Falta implementar paginación

        //Traemos todas las actividades publicadas y activas (temporalmente ignoramos los filtros)
        List<Actividad> actividades = actividadRepository.explorarActividadesPublicadas();

        return actividades.stream()
                .map(actividadMapper::actividadToDTOListadoActividadVisitante)
                .toList();
    }

    //US-ACT-04: Modificar Actividad
    @Transactional
    public DTOActividadGetResponse modificarActividad(UUID idActividad, DTOActividadUpdate dto) {

        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada "));

        if (actividad.getFechaHoraBaja() != null) {
            throw new ValidacionNegocioException("No se puede modificar una actividad que ya está dada de baja");
        }

        // TODO: No se permite cambiar a estado borrador cuando hay reservas en estado pendiente o pagada
        /*if (EstadoActividadNombre.BORRADOR.name().equalsIgnoreCase(dto.getEstado()) && reservasTotales > 0) {
            throw new ValidacionNegocioException("No se puede guardar como borrador: hay reservas asociadas.");
        }*/
        //Primero hacemos las validaciones del negocio
        List<String> errores = actividadValidaciones.obtenerErroresValidacionModificacion(idActividad, dto);

        if (!errores.isEmpty()) {
            throw new ValidacionMultipleException(errores);
        }

        actividad.setNombre(dto.getNombre());
        actividad.setDescripcion(dto.getDescripcion());
        List<ActividadRangoEtario> activosActuales = actividad.getActividadRangoEtarios().stream()
                .filter(r -> r.getFechaHoraBaja() == null)
                .collect(Collectors.toList());

        List <ActividadRangoEtario> nuevasTarifas = actualizarTarifas(dto.getTarifas(), activosActuales);

        nuevasTarifas.forEach(actividad::addActividadRangoEtario);

        actividad.getInclusiones().clear();
        List<ActividadInclusiones> nuevasInclusiones = obtenerInclusiones(dto.getIncluye(), dto.getNoIncluye());
        actividad.getInclusiones().addAll(nuevasInclusiones);

        actividad.getFaqs().clear();
        List<ActividadFAQ> nuevasFaqs = obtenerFaqs(dto.getFaqs());
        actividad.getFaqs().addAll(nuevasFaqs);

        Actividad actividadGuardada = actividadRepository.save(actividad);
        DTOActividadGetResponse response = actividadMapper.actividadToDTOActividadGetResponse(actividadGuardada);
        List<String> advertencias = calcularHuecos(dto.getTarifas());
        response.setAdvertencias(advertencias);
        return response;

    }
    @Transactional(readOnly = true)
    public DTOActividadGetResponse obtenerActividadPorId(UUID idActividad) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        if (actividad.getFechaHoraBaja() != null) {
            throw new ValidacionNegocioException("La actividad solicitada se encuentra dada de baja y no puede ser editada.");
        }

        return actividadMapper.actividadToDTOActividadGetResponse(actividad);
    }

    //Métodos auxiliares

    private EstadoActividad obtenerEstado(DTOActividadAlta dto) {
        EstadoActividadNombre estadoActividadNombre;
        try {
            estadoActividadNombre = EstadoActividadNombre.valueOf(dto.getEstado().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DatoInvalidoException("El estado de actividad proporcionado es inválido: " + dto.getEstado());
        }
        return estadoActividadRepository.findByNombre(estadoActividadNombre)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el registro del estado " + estadoActividadNombre + " en la base de datos."));
    }

    private ActividadLogAltas obtenerLogAltas(DTOActividadAlta dto) {
        ActividadLogAltas logAltas = new ActividadLogAltas();
        logAltas.setFechaHoraAlta(LocalDateTime.now());
        logAltas.setFechaValidaDesde(dto.getFechaDesde());
        logAltas.setFechaValidaHasta(dto.getFechaHasta());

        if (dto.getDiasDisponibles() != null) {
            // Recorremos los días que el usuario seleccionó en la pantalla
            for (DTODiaDisponibilidad diaDto : dto.getDiasDisponibles()) {
                ActividadLogAltasDia dia = new ActividadLogAltasDia();
                dia.setDia(diaDto.getDia());
                dia.setHoraInicio(diaDto.getHoraInicio());
                dia.setHoraFin(diaDto.getHoraFin());
                logAltas.addDia(dia);
            }
        }
        return logAltas;
    }

    //Método para crear las ActividadDia
    private List<ActividadDia> generarDiasCalendario(DTOActividadAlta dto, ActividadLogAltas logAltas) {
        List<ActividadDia> diasGenerados = new ArrayList<>();
        LocalDate fechaActual = dto.getFechaDesde();
        LocalDate limite = dto.getFechaHasta();

        EstadoActividadDia estadoActivaEntidad = estadoActividadDiaRepository.findByNombre(EstadoActividadDiaNombre.ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("El estado ACTIVA no está configurado en la base de datos de catálogos."));

        while (!fechaActual.isAfter(limite)) {
            java.time.DayOfWeek diaSemanaActual = fechaActual.getDayOfWeek();

            for (ActividadLogAltasDia configDia : logAltas.getDias()) {
                if (coincideDia(configDia.getDia(), diaSemanaActual)) {

                    // Calculamos la fecha y hora exacta de inicio de este turno
                    LocalDateTime inicioCalculado = LocalDateTime.of(fechaActual, configDia.getHoraInicio());

                    // Evitar crear disponibilidades cuya hora de inicio ya haya pasado.
                    if (!inicioCalculado.isAfter(LocalDateTime.now())) {
                        continue; // Salta este horario y sigue buscando
                    }

                    ActividadDia actividadDia = new ActividadDia();
                    actividadDia.setFechaHoraInicio(LocalDateTime.of(fechaActual, configDia.getHoraInicio()));
                    actividadDia.setFechaHoraFin(LocalDateTime.of(fechaActual, configDia.getHoraFin()));
                    actividadDia.setCuposMax(dto.getCuposMax());

                    ActividadDiaEstado estadoInicial = new ActividadDiaEstado();
                    estadoInicial.setEstado(estadoActivaEntidad);
                    estadoInicial.setFechaHoraInicio(LocalDateTime.now());
                    actividadDia.registrarNuevoEstado(estadoInicial);

                    configDia.addActividadDia(actividadDia);
                    diasGenerados.add(actividadDia);
                    break;
                }
            }
            fechaActual = fechaActual.plusDays(1);
        }

        if (diasGenerados.isEmpty()) {
            throw new ValidacionNegocioException(" El rango de fechas seleccionado ("
                    + dto.getFechaDesde() + " al " + dto.getFechaHasta() +
                    ") no contiene ninguno de los días de la semana configurados.");
        }
        return diasGenerados;
    }

    private boolean coincideDia(Dia diaEnum, java.time.DayOfWeek dayOfWeek) {
        return switch (diaEnum) {
            case LUNES -> dayOfWeek == java.time.DayOfWeek.MONDAY;
            case MARTES -> dayOfWeek == java.time.DayOfWeek.TUESDAY;
            case MIERCOLES -> dayOfWeek == java.time.DayOfWeek.WEDNESDAY;
            case JUEVES -> dayOfWeek == java.time.DayOfWeek.THURSDAY;
            case VIERNES -> dayOfWeek == java.time.DayOfWeek.FRIDAY;
            case SABADO -> dayOfWeek == java.time.DayOfWeek.SATURDAY;
            case DOMINGO -> dayOfWeek == java.time.DayOfWeek.SUNDAY;
            default -> false;
        };
    }

    public List <ActividadRangoEtario> actualizarTarifas(List<DTOTarifa> nuevosRangos,List<ActividadRangoEtario> activosActuales) {

        List<ActividadRangoEtario> aDarDeBaja = new ArrayList<>();
        List<DTOTarifa> aInsertar = new ArrayList<>(nuevosRangos);

        for (ActividadRangoEtario activo : activosActuales) {
            // Buscamos si en el DTO viene un rango exactamente igual al activo
            Optional<DTOTarifa> matchExacto = aInsertar.stream()
                    .filter(dto -> dto.getNombre().equals(activo.getNombre()) &&
                            dto.getEdadMinima().equals(activo.getEdadMinima()) &&
                            dto.getEdadMaxima().equals(activo.getEdadMaxima()) &&
                            dto.isEsTarifaBase() == activo.isEsTarifaBase() &&
                            dto.getPrecio().compareTo(activo.getPrecio()) == 0)
                    .findFirst();

            if (matchExacto.isPresent()) {
                // Está intacto. Lo quitamos de "aInsertar" para no duplicarlo.
                aInsertar.remove(matchExacto.get());
            } else {
                // Fue modificado o eliminado. Lo marcamos para baja lógica.
                aDarDeBaja.add(activo);
            }
        }

        LocalDateTime ahora = LocalDateTime.now();
        aDarDeBaja.forEach(r -> r.setFechaHoraBaja(ahora));

        return  obtenerTarifas(aInsertar);
    }
    //Paso 3: Participanetes y tarifas
    private List<ActividadRangoEtario> obtenerTarifas(List<DTOTarifa> dtosTarifa) {
        if (dtosTarifa == null || dtosTarifa.isEmpty()) {
            return Collections.emptyList();
        }

        List<ActividadRangoEtario> tarifas = new ArrayList<>();

        for (DTOTarifa tarifaDto : dtosTarifa) {

            ActividadRangoEtario tarifa = new ActividadRangoEtario();
            tarifa.setNombre(tarifaDto.getNombre());
            tarifa.setPrecio(tarifaDto.getPrecio());
            tarifa.setEdadMinima(tarifaDto.getEdadMinima());
            tarifa.setEdadMaxima(tarifaDto.getEdadMaxima());
            tarifa.setEsTarifaBase(tarifaDto.isEsTarifaBase());
            tarifas.add(tarifa);
        }

        return tarifas;
    }

    private List<ActividadInclusiones> obtenerInclusiones(List<String> incluye, List<String> noIncluye) {
        List<ActividadInclusiones> inclusiones = new ArrayList<>();
        if (incluye != null) {
            for (String desc : incluye) {
                ActividadInclusiones inclusion = new ActividadInclusiones();
                inclusion.setDescripcion(desc);
                inclusion.setIncluye(true);
                inclusiones.add(inclusion);
            }
        }
        if (noIncluye != null) {
            for (String desc : noIncluye) {
                ActividadInclusiones exclusion = new ActividadInclusiones();
                exclusion.setDescripcion(desc);
                exclusion.setIncluye(false);
                inclusiones.add(exclusion);
            }
        }
        return inclusiones;
    }

    private List<ActividadFAQ> obtenerFaqs(List<DTOFaq> dtosFaq) {
        if (dtosFaq == null) {
            return Collections.emptyList();
        }

        List<ActividadFAQ> faqs = new ArrayList<>();

        for (DTOFaq faqDto : dtosFaq) {
            ActividadFAQ faq = new ActividadFAQ();
            faq.setPregunta(faqDto.getPregunta());
            faq.setRespuesta(faqDto.getRespuesta());
            faqs.add(faq);
        }
        return faqs;
    }

    private List<String> calcularHuecos(List<DTOTarifa> tarifas) {

        List<String> huecos = new ArrayList<>();

        if (tarifas == null || tarifas.isEmpty()) {
            huecos.add("0 a 120 años");
            return huecos;
        }

        // Ordenamos por edad mínima de menor a mayor
        List<DTOTarifa> tarifasOrdenadas = new ArrayList<>(tarifas);
        tarifasOrdenadas.sort(Comparator.comparingInt(DTOTarifa::getEdadMinima));

        // Verificamos el hueco inicial
        DTOTarifa primerRango = tarifasOrdenadas.get(0);
        if (primerRango.getEdadMinima() > 0) {
            int finHueco = primerRango.getEdadMinima() - 1;
            huecos.add("0 a " + finHueco + " años");
        }

        // Verificamos los huecos intermedios
        for (int i = 0; i < tarifasOrdenadas.size() - 1; i++) {
            int maxActual = tarifasOrdenadas.get(i).getEdadMaxima();
            int minSiguiente = tarifasOrdenadas.get(i + 1).getEdadMinima();

            if (minSiguiente > maxActual + 1) {
                int inicioHueco = maxActual + 1;
                int finHueco = minSiguiente - 1;
                huecos.add(inicioHueco + " a " + finHueco + " años");
            }
        }

        // Verificamos el hueco final
        DTOTarifa ultimoRango = tarifasOrdenadas.get(tarifasOrdenadas.size() - 1);
        //TODO: Setear como parámetro global del sistema
        int EDAD_MAXIMA_SISTEMA = 120;

        if (ultimoRango.getEdadMaxima() < EDAD_MAXIMA_SISTEMA) {
            int inicioHueco = ultimoRango.getEdadMaxima() + 1;
            huecos.add(inicioHueco + " a " + EDAD_MAXIMA_SISTEMA + " años");
        }

        return huecos;
    }



}





