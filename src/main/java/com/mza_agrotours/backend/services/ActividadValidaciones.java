package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOActividadAlta;
import com.mza_agrotours.backend.dtos.actividad.DTODiaDisponibilidad;
import com.mza_agrotours.backend.dtos.actividad.DTOTarifa;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.exceptions.actividad.ActividadAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.repositories.actividad.ActividadRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component

public class ActividadValidaciones {

    @Autowired
    private ActividadRespository actividadRepository;

    public void validarAlta(DTOActividadAlta dto){

        // Validar que  no haya dos actividades con mismo nombre
        if (actividadRepository.existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(dto.getNombre())) {
            throw new ActividadAlreadyExistsException("Ya existe una actividad con este nombre");
        }

        // No permitir crear una actividad con estado "Dado de baja"
        String estadoRecibido = dto.getEstado();

        if (estadoRecibido == null ||
                (!EstadoActividadNombre.BORRADOR.name().equalsIgnoreCase(estadoRecibido) &&
                !EstadoActividadNombre.PUBLICADO.name().equalsIgnoreCase(estadoRecibido))) {
            throw new ValidacionNegocioException("Una actividad nueva no puede crearse en estado '" + dto.getEstado() + "'. Solo se permite en estado BORRADOR o PUBLICADO.");
        }
        validarFechas(dto);
        validarTarifas(dto);
        validarDisponibilidad(dto);

    }

    private void validarFechas(DTOActividadAlta dto) {
        long diasDiferencia = ChronoUnit.DAYS.between(dto.getFechaDesde(), dto.getFechaHasta());

        //Validar que la fecha desde sea igual o anterior que fecha hasta
        if (diasDiferencia < 0) {
            throw new ValidacionNegocioException("La fecha 'hasta' debe ser igual o posterior a la fecha 'desde'.");
        }

        //Validar que la vigencia de la actividad sea mayor a 0 y menor a 120 días
        if (diasDiferencia > 120) {
            throw new ValidacionNegocioException("La vigencia máxima permitida es de 120 días corridos.");
        }
    }

    //Validar que no se crear múltiple actividadRangoEtario para un mismo rango etario
    private void validarTarifas(DTOActividadAlta dto) {
        if (dto.getTarifas() == null ) {
            throw new ValidacionNegocioException("Debes cargar al menos una tarifa para la actividad.");
        }

        long rangosUnicos = dto.getTarifas().stream()
                    .map(DTOTarifa::getRangoEtarioId)
                    .distinct()
                    .count();

        if (rangosUnicos < dto.getTarifas().size()) {
            throw new ValidacionNegocioException("No se pueden asignar múltiples tarifas para un mismo rango etario.");

        }

    }


    private void validarDisponibilidad(DTOActividadAlta dto) {

        //Validar que se cree solo un horario para un dia
        if (dto.getDiasDisponibles() != null) {
            long diasUnicos = dto.getDiasDisponibles().stream()
                    .map(DTODiaDisponibilidad::getDia)
                    .distinct()
                    .count();

            if (diasUnicos < dto.getDiasDisponibles().size()) {
                throw new ValidacionNegocioException("No se permiten días duplicados. Se admite una única franja horaria por día.");
            }

            //Validar que que fecha hora inicio sea anterior a fecha hora fin
            for (DTODiaDisponibilidad configDia : dto.getDiasDisponibles()) {
                if (!configDia.getHoraInicio().isBefore(configDia.getHoraFin())) {
                    throw new ValidacionNegocioException("Error en la configuración del día " + configDia.getDia() +
                            ": La hora de inicio debe ser anterior a la hora de fin.");
                }
            }
        }
    }


}
