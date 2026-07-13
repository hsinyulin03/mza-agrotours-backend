package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOActividadAlta;
import com.mza_agrotours.backend.dtos.actividad.DTODiaDisponibilidad;
import com.mza_agrotours.backend.dtos.actividad.DTOTarifa;
import com.mza_agrotours.backend.enums.EstadoActividad;
import com.mza_agrotours.backend.repositories.ActividadRespository;
import lombok.RequiredArgsConstructor;
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
            throw new RuntimeException("Ya existe una actividad con este nombre");
        }
        // No permitir crear una actividad con estado "Dado de baja"
        if (dto.getEstado() == EstadoActividad.DADO_DE_BAJA) {
            throw new RuntimeException("Error de validación: Una actividad nueva no puede crearse en estado DADO_DE_BAJA. Solo se permite crearse en estado BORRADOR o PUBLICADO.");
        }

        validarFechas(dto);
        validarTarifas(dto);
        validarDisponibilidad(dto);

    }

    private void validarFechas(DTOActividadAlta dto) {
        long diasDiferencia = ChronoUnit.DAYS.between(dto.getFechaDesde(), dto.getFechaHasta());

        //Validar que la fecha desde sea igual o anterior que fecha hasta
        if (diasDiferencia < 0) {
            throw new RuntimeException("La fecha 'hasta' debe ser igual o posterior a la fecha 'desde'.");
        }

        //Validar que la vigencia de la actividad sea mayor a 0 y menor a 120 días
        if (diasDiferencia > 120) {
            throw new RuntimeException("La vigencia máxima permitida es de 120 días corridos.");
        }
    }

    //Validar que no se crear múltiple actividadRangoEtario para un mismo rango etario
    private void validarTarifas(DTOActividadAlta dto) {
        if (dto.getTarifas() != null) {
            long rangosUnicos = dto.getTarifas().stream()
                    .map(DTOTarifa::getRangoEtarioId)
                    .distinct()
                    .count();

            if (rangosUnicos < dto.getTarifas().size()) {
                throw new RuntimeException("Error de validación: No se pueden asignar múltiples tarifas para un mismo rango etario.");
            }
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
                throw new RuntimeException("Error de validación: No se permiten días duplicados. Se admite una única franja horaria por día.");
            }

            //Validar que que fecha hora inicio sea anterior a fecha hora fin
            for (DTODiaDisponibilidad configDia : dto.getDiasDisponibles()) {
                if (!configDia.getHoraInicio().isBefore(configDia.getHoraFin())) {
                    throw new RuntimeException("Error en la configuración del día " + configDia.getDia() +
                            ": La hora de inicio debe ser anterior a la hora de fin.");
                }
            }
        }
    }


}
