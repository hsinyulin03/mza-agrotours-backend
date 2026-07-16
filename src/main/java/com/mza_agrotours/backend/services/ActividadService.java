package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.entities.RangoEtario;
import com.mza_agrotours.backend.entities.actividad.*;
import com.mza_agrotours.backend.enums.Dia;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.exceptions.ResourceNotFoundException;
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.mappers.ActividadMapper;
import com.mza_agrotours.backend.repositories.actividad.ActividadRespository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadDiaRepository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadRepository;
import com.mza_agrotours.backend.repositories.rangoEtario.RangoEtarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class ActividadService {

    @Autowired
    private ActividadRespository actividadRepository;

    @Autowired
    private RangoEtarioRepository rangoEtarioRepository;

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
    public DTOActividadDetalle altaActividad(DTOActividadAlta dto) {

            //Primero hacemos las validaciones del negocio
            actividadValidaciones.validarAlta(dto);

            final EstadoActividadNombre estadoActividadNombre;

            try {
                estadoActividadNombre = EstadoActividadNombre.valueOf(dto.getEstado().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new DatoInvalidoException("El estado de actividad proporcionado es inválido: " + dto.getEstado());
            }

            EstadoActividad estado = estadoActividadRepository.findByNombre(estadoActividadNombre)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró el registro del estado " + estadoActividadNombre+ " en la base de datos."));

            //Paso 1: Información General
            Actividad actividad = new Actividad();
            actividad.setNombre(dto.getNombre());
            actividad.setDescripcion(dto.getDescripcion());
            actividad.setCuposMax(dto.getCuposMax());
            //Guarda el UUID del estado
            actividad.setEstado(estado);


            agregarInclusiones(actividad, dto);
            agregarFaqs(actividad, dto);
            agregarTarifas(actividad, dto);
            ActividadLogAltas logAltas = configurarLogAltas(actividad, dto);
            generarCalendario(actividad, dto, logAltas);


            // Persistir en la base de datos
            Actividad actividadGuardada = actividadRepository.save(actividad);
            return actividadMapper.actividadToDetalleDto(actividadGuardada);

    }

    //Paso 2: Detalles de la experiencia (Qué incluye/ qué no incluye / FAQ)
    private void agregarInclusiones(Actividad actividad, DTOActividadAlta dto) {
        if (dto.getIncluye() != null) {
            dto.getIncluye().forEach(desc -> {
                ActividadInclusiones inclusion = new ActividadInclusiones();
                inclusion.setDescripcion(desc);
                inclusion.setIncluye(true);
                actividad.addInclusion(inclusion);
            });
        }
        if (dto.getNoIncluye() != null) {
            dto.getNoIncluye().forEach(desc -> {
                ActividadInclusiones exclusion = new ActividadInclusiones();
                exclusion.setDescripcion(desc);
                exclusion.setIncluye(false);
                actividad.addInclusion(exclusion);
            });
        }
    }

    private void agregarFaqs(Actividad actividad, DTOActividadAlta dto) {
        if (dto.getFaqs() != null) {
            dto.getFaqs().forEach(faqDto -> {
                ActividadFAQ faq = new ActividadFAQ();
                faq.setPregunta(faqDto.getPregunta());
                faq.setRespuesta(faqDto.getRespuesta());
                actividad.addFaq(faq);
            });
        }
    }


    //Paso 3: Participanetes y tarifas
    private void agregarTarifas(Actividad actividad, DTOActividadAlta dto) {


        int contadorTarifasBase = 0;

        for (DTOTarifa tarifaDto : dto.getTarifas()) {
                RangoEtario rango = rangoEtarioRepository.findByIdAndFechaHoraBajaIsNull(tarifaDto.getRangoEtarioId())
                        .orElseThrow(() -> new ResourceNotFoundException("El rango etario con ID " + tarifaDto.getRangoEtarioId() + " no existe"));

                if (tarifaDto.isEsTarifaBase()) {
                     contadorTarifasBase++;
                 }

                ActividadRangoEtario tarifa = new ActividadRangoEtario();
                tarifa.setPrecio(tarifaDto.getPrecio());
                tarifa.setRangoEtario(rango);
                tarifa.setEsTarifaBase(tarifaDto.isEsTarifaBase());
                tarifa.setFechaValidaDesde(LocalDate.now());
                actividad.addActividadRangoEtario(tarifa);
        }
        if (contadorTarifasBase == 0) {
            throw new ValidacionNegocioException("Es obligatorio marcar un rango etario como tarifa base.");
        } else if (contadorTarifasBase > 1) {
            throw new ValidacionNegocioException("No puedes tener más de una tarifa base en la misma actividad.");
        }
    }


    //Paso 4: Disponibilidad

    private ActividadLogAltas configurarLogAltas(Actividad actividad, DTOActividadAlta dto) {
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
            actividad.addLogAlta(logAltas);
        }
        return logAltas;
    }

    //Método para crear las ActividadDia
    private void generarCalendario(Actividad actividad, DTOActividadAlta dto, ActividadLogAltas logAltas) {
        if (logAltas.getDias() == null || logAltas.getDias().isEmpty()) {
            throw new ValidacionNegocioException("Debe configurar al menos un día y horario de disponibilidad para la actividad.");
        }
        LocalDate fechaActual = dto.getFechaDesde();
        LocalDate limite = dto.getFechaHasta();
        int cantidadDiasGenerados = 0; //Contador para asegurar que mínimo se genere un ActividadDia

        EstadoActividadDia estadoActivaEntidad = estadoActividadDiaRepository.findByNombre(EstadoActividadDiaNombre.ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("El estado ACTIVA no está configurado en la base de datos de catálogos."));

        while (!fechaActual.isAfter(limite)) {
            java.time.DayOfWeek diaSemanaActual = fechaActual.getDayOfWeek();

            for (ActividadLogAltasDia configDia : logAltas.getDias()) {
                if (coincideDia(configDia.getDia(), diaSemanaActual)) {
                    ActividadDia actividadDia = new ActividadDia();
                    actividadDia.setFechaHoraInicio(LocalDateTime.of(fechaActual, configDia.getHoraInicio()));
                    actividadDia.setFechaHoraFin(LocalDateTime.of(fechaActual, configDia.getHoraFin()));
                    actividadDia.setCuposMax(dto.getCuposMax());

                    ActividadDiaEstado estadoInicial = new ActividadDiaEstado();
                    estadoInicial.setEstado(estadoActivaEntidad);
                    estadoInicial.setFechaHoraInicio(LocalDateTime.now());
                    actividadDia.registrarNuevoEstado(estadoInicial);

                    actividad.addActividadDia(actividadDia);
                    configDia.addActividadDia(actividadDia);
                    cantidadDiasGenerados++;
                    break;
                }
            }
            fechaActual = fechaActual.plusDays(1);
        }
        if (cantidadDiasGenerados == 0) {
            throw new ValidacionNegocioException(" El rango de fechas seleccionado ("
                    + dto.getFechaDesde() + " al " + dto.getFechaHasta() +
                    ") no contiene ninguno de los días de la semana configurados.");
        }
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

    //US-ACT-02:  Consultar detalle de una actividad
    @Transactional(readOnly = true)
    public DTOActividadDetalle obtenerDetallePorId(UUID id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada con ID: " + id));

        if (actividad.getFechaHoraBaja() != null) {
            throw new ValidacionNegocioException("La actividad se encuentra dada de baja");
        }
        return actividadMapper.actividadToDetalleDto(actividad);
    }

    //US-ACT-06: Listado de actividades de un establecimiento - Vista productor
    @Transactional(readOnly = true)
    public List<DTOActividades> obtenerListadoActividades(String busqueda, EstadoActividadNombre estado) {
        // TODO- Se debe filtrar por establecimiento
        List<Actividad> actividades = actividadRepository.findByFiltrosDinamicos(busqueda, estado);

        return actividades.stream()
                .map(actividadMapper::actividadToDTOActividades)
                .toList();
    }

    //US-ACT-07: Consultar todos los días disponibles para una actividad
    @Transactional(readOnly = true)
    public DTOCalendarioActividadDia obtenerDetalleCalendario(UUID actividadId, int mes, int anio){

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

        DTOCalendarioActividadDia dto = actividadMapper.actividadToDTOCalendarioActividadDia(actividad);

        List<DTOActividadDia> diasDelMesDto = actividad.getActividadesDias().stream()
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
    public List<DTOListadoActividadVisitante> explorarActividades() {

        // TODO: Falta implementar filtro por cultivo y por departamento
        // TODO: Falta implementar paginación

        //Traemos todas las actividades publicadas y activas (temporalmente ignoramos los filtros)
        List<Actividad> actividades = actividadRepository.explorarActividadesPublicadas();

        return actividades.stream()
                .map(actividadMapper::actividadToDTOListadoActividadVisitante)
                .toList();
    }


}





