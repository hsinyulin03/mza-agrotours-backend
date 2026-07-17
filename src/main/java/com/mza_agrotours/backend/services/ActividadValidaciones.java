package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOActividadAlta;
import com.mza_agrotours.backend.dtos.actividad.DTODiaDisponibilidad;
import com.mza_agrotours.backend.dtos.actividad.DTOTarifa;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.repositories.actividad.ActividadRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component

public class ActividadValidaciones {

    @Autowired
    private ActividadRespository actividadRepository;

    public List<String> obtenerErroresValidacionActividad(DTOActividadAlta dto){

        List<String> errores = new ArrayList<>();

        // Validar que  no haya dos actividades con mismo nombre
        if (actividadRepository.existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(dto.getNombre())) {
            errores.add("Ya existe una actividad con este nombre");
        }

        // No permitir crear una actividad con estado "Dado de baja"
        String estadoRecibido = dto.getEstado();

        if (estadoRecibido == null ||
                (!EstadoActividadNombre.BORRADOR.name().equalsIgnoreCase(estadoRecibido) &&
                !EstadoActividadNombre.PUBLICADO.name().equalsIgnoreCase(estadoRecibido))) {
            errores.add("Una actividad nueva no puede crearse en estado '" + dto.getEstado() + "'. Solo se permite en estado BORRADOR o PUBLICADO.");
        }
        errores.addAll(validarFechas(dto));
        errores.addAll(validarTarifas(dto));
        errores.addAll(validarDisponibilidad(dto));

        return errores;
    }

    private List<String> validarFechas(DTOActividadAlta dto) {
        List<String> errores = new ArrayList<>();
        long diasDiferencia = ChronoUnit.DAYS.between(dto.getFechaDesde(), dto.getFechaHasta());

        //Validar que la fecha desde sea igual o anterior que fecha hasta
        if (diasDiferencia < 0) {
            errores.add("La fecha 'hasta' debe ser igual o posterior a la fecha 'desde'.");
        }

        //Validar que la vigencia de la actividad sea mayor a 0 y menor a 120 días
        if (diasDiferencia > 120) {
            errores.add("La vigencia máxima permitida es de 120 días corridos.");
        }
        return errores;
    }

    //Validar que no se crear múltiple actividadRangoEtario para un mismo rango etario
    private List<String> validarTarifas(DTOActividadAlta dto) {

        List<String> errores = new ArrayList<>();

        if (dto.getTarifas() == null ) {
            errores.add("Debes cargar al menos una tarifa para la actividad.");
        }

        long rangosUnicos = dto.getTarifas().stream()
                    .map(DTOTarifa::getRangoEtarioId)
                    .distinct()
                    .count();

        if (rangosUnicos < dto.getTarifas().size()) {
            errores.add("No se pueden asignar múltiples tarifas para un mismo rango etario.");

        }

        long contadorTarifasBase = dto.getTarifas().stream().filter(DTOTarifa::isEsTarifaBase).count();
        if (contadorTarifasBase == 0) {
            errores.add("Es obligatorio marcar un rango etario como tarifa base.");
        } else if (contadorTarifasBase > 1) {
            errores.add("No puedes tener más de una tarifa base en la misma actividad.");
        }

        return errores;
    }


    private List<String> validarDisponibilidad(DTOActividadAlta dto) {

        List<String> errores = new ArrayList<>();

        if (dto.getDiasDisponibles() == null || dto.getDiasDisponibles().isEmpty()) {
            errores.add("Debe configurar al menos un día y horario de disponibilidad para la actividad.");
            return errores;
        }

        //Validar que se cree solo un horario para un dia

        long diasUnicos = dto.getDiasDisponibles().stream()
                .map(DTODiaDisponibilidad::getDia)
                .distinct()
                .count();

        if (diasUnicos < dto.getDiasDisponibles().size()) {
            errores.add("No se permiten días duplicados. Se admite una única franja horaria por día.");
        }

        //Validar que que fecha hora inicio sea anterior a fecha hora fin
        for (DTODiaDisponibilidad configDia : dto.getDiasDisponibles()) {
            if (!configDia.getHoraInicio().isBefore(configDia.getHoraFin())) {
                errores.add("Error en la configuración del día " + configDia.getDia() +
                        ": La hora de inicio debe ser anterior a la hora de fin.");
            }
        }
        return errores;
    }


}
