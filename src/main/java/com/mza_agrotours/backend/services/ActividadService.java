package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.entities.RangoEtario;
import com.mza_agrotours.backend.entities.actividad.*;
import com.mza_agrotours.backend.enums.Dia;
import com.mza_agrotours.backend.enums.EstadoActividadDia;
import com.mza_agrotours.backend.mappers.ActividadMapper;
import com.mza_agrotours.backend.repositories.ActividadRespository;
import com.mza_agrotours.backend.repositories.RangoEtarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ActividadService extends BaseEntityServiceImpl<Actividad, Long> {

    @Autowired
    private ActividadRespository actividadRepository;

    @Autowired
    private RangoEtarioRepository rangoEtarioRepository;

    @Autowired
    private ActividadValidaciones actividadValidaciones;

    @Autowired
    private ActividadMapper actividadMapper;

    //US-ACT-03 Alta de actividad
    @Transactional
    public void altaActividad(DTOActividadAlta dto) {

        try {

            //Paso 1: Información General
            Actividad actividad = new Actividad();
            actividad.setNombre(dto.getNombre());
            actividad.setDescripcion(dto.getDescripcion());
            actividad.setCuposMax(dto.getCuposMax());
            actividad.setEstado(dto.getEstado());


            //Primero hacemos las validaciones del negocio
            actividadValidaciones.validarAlta(dto);

            agregarInclusiones(actividad, dto);
            agregarFaqs(actividad, dto);
            agregarTarifas(actividad, dto);
            ActividadLogAltas logAltas = configurarLogAltas(actividad, dto);
            generarCalendario(actividad, dto, logAltas);


            // Persistir en la base de datos
            actividadRepository.save(actividad);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());

        }

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


    //Paso 3: Participanetes y taridas
    private void agregarTarifas(Actividad actividad, DTOActividadAlta dto) {

        boolean tieneTarifaBase = false;

        if (dto.getTarifas() != null) {
            for (DTOTarifa tarifaDto : dto.getTarifas()) {
                RangoEtario rango = rangoEtarioRepository.findByIdAndFechaHoraBajaIsNull(tarifaDto.getRangoEtarioId())
                        .orElseThrow(() -> new RuntimeException("El rango etario con ID " + tarifaDto.getRangoEtarioId() + " no existe"));

                //La tarifa para adultos es obligatorio
                if (rango.isEsTarifaBase()) {
                    tieneTarifaBase = true;
                }

                ActividadRangoEtario tarifa = new ActividadRangoEtario();
                tarifa.setPrecio(tarifaDto.getPrecio());
                tarifa.setRangoEtario(rango);
                tarifa.setFechaValidaDesde(LocalDate.now());
                actividad.addActividadRangoEtario(tarifa);
            }
        }

        if (!tieneTarifaBase) {
            throw new RuntimeException("La tarifa para Adultos es obligatoria.");
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

    //Método para crear los ActividadDia
    private void generarCalendario(Actividad actividad, DTOActividadAlta dto, ActividadLogAltas logAltas) {

        if (logAltas.getDias() == null || logAltas.getDias().isEmpty()) {
            throw new RuntimeException("Debe configurar al menos un día y horario de disponibilidad para la actividad.");
        }
        LocalDate fechaActual = dto.getFechaDesde();
        LocalDate limite = dto.getFechaHasta();
        int cantidadDiasGenerados = 0; //Contador para asegurar que mínimo se genere un ActividadDia


        while (!fechaActual.isAfter(limite)) {
            java.time.DayOfWeek diaSemanaActual = fechaActual.getDayOfWeek();

            for (ActividadLogAltasDia configDia : logAltas.getDias()) {
                if (coincideDia(configDia.getDia(), diaSemanaActual)) {
                    ActividadDia actividadDia = new ActividadDia();
                    actividadDia.setFechaHoraInicio(LocalDateTime.of(fechaActual, configDia.getHoraInicio()));
                    actividadDia.setFechaHoraFin(LocalDateTime.of(fechaActual, configDia.getHoraFin()));
                    actividadDia.setCuposMax(dto.getCuposMax());
                    actividadDia.setLogAltasDia(configDia);

                    ActividadDiaEstado estadoInicial = new ActividadDiaEstado();
                    estadoInicial.setEstado(EstadoActividadDia.ACTIVA);
                    estadoInicial.setFechaHoraInicio(LocalDateTime.now());
                    actividadDia.registrarNuevoEstado(estadoInicial);

                    actividad.addActividadDia(actividadDia);
                    cantidadDiasGenerados++;
                    break;
                }
            }
            fechaActual = fechaActual.plusDays(1);
        }
        if (cantidadDiasGenerados == 0) {
            throw new RuntimeException(" El rango de fechas seleccionado ("
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
    @Transactional
    public DTOActividadDetalle obtenerDetallePorId(Long id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada con ID: " + id));

        if (actividad.getFechaHoraBaja() != null) {
            throw new RuntimeException("La actividad se encuentra dada de baja");
        }
        return actividadMapper.actividadToDetalleDto(actividad);
    }

    //US-ACT-06: US-ACT-06: Listado de actividades de un establecimiento - Vista productor
    public List<DTOActividades> obtenerListadoActividades() {
        // TODO- Se debe filtrar por establecimiento
        List<Actividad> actividades = actividadRepository.findAll();

        return actividades.stream()
                .map(actividadMapper::actividadToActividadesDto)
                .toList();
    }

    //US-ACT-07: Consultar todos los días disponibles para una actividad
    public DTOCalendarioActividadDia obtenerDetalleCalendario(Long actividadId, int mes, int anio) throws Exception {

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        DTOCalendarioActividadDia dto = actividadMapper.actividadToDTOCalendarioActividadDia(actividad);

        List<DTOActividadDia> diasDelMesDto = new java.util.ArrayList<>();

        for (ActividadDia dia : actividad.getActividadesDias()) {

            // Filtramos los días que corresponden al mes y al año que el usuario desea ver
            if (dia.getFechaHoraInicio().getYear() == anio && dia.getFechaHoraInicio().getMonthValue() == mes) {

                DTOActividadDia diaDto = new DTOActividadDia();
                diaDto.setFecha(dia.getFechaHoraInicio().toLocalDate());
                diaDto.setCuposMaximos(dia.getCuposMax());

                if (dia.getEstadoActual() != null) {
                    diaDto.setEstadoActual(dia.getEstadoActual().getEstado().name());
                }

                diasDelMesDto.add(diaDto);
            }
        }

        dto.setDiasDelMes(diasDelMesDto);

        return dto;
    }

    //US-ACT-12: Listado de actividades de la plataforma - vista del visitante
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





