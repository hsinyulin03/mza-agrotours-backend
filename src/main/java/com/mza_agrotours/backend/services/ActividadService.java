package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOActividadAlta;
import com.mza_agrotours.backend.dtos.actividad.DTODiaDisponibilidad;
import com.mza_agrotours.backend.dtos.actividad.DTOTarifa;
import com.mza_agrotours.backend.entities.RangoEtario;
import com.mza_agrotours.backend.entities.actividad.*;
import com.mza_agrotours.backend.enums.Dia;
import com.mza_agrotours.backend.enums.EstadoActividadDia;
import com.mza_agrotours.backend.repositories.ActividadRespository;
import com.mza_agrotours.backend.repositories.RangoEtarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class ActividadService  {

    @Autowired
    private ActividadRespository actividadRepository;

    @Autowired
    private RangoEtarioRepository rangoEtarioRepository;

    @Autowired
    private ActividadValidaciones actividadValidaciones;



    @Transactional
    public void AltaActividad(DTOActividadAlta dto) {

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

        boolean tieneAdulto = false;

        if (dto.getTarifas() != null) {
            for (DTOTarifa tarifaDto : dto.getTarifas()) {
                RangoEtario rango = rangoEtarioRepository.findByIdAndFechaHoraBajaIsNull(tarifaDto.getRangoEtarioId())
                        .orElseThrow(() -> new RuntimeException("El rango etario con ID " + tarifaDto.getRangoEtarioId() + " no existe"));

                //La tarifa para adultos es obligatorio
                if (rango.getNombre().toLowerCase().contains("adulto")) {
                    tieneAdulto = true;
                }

                ActividadRangoEtario tarifa = new ActividadRangoEtario();
                tarifa.setPrecio(tarifaDto.getPrecio());
                tarifa.setRangoEtario(rango);
                tarifa.setFechaValidaDesde(LocalDateTime.now());
                actividad.addActividadRangoEtario(tarifa);
            }
        }

        if (!tieneAdulto) {
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

}



