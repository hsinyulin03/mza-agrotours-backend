package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOActividadAlta;
import com.mza_agrotours.backend.dtos.actividad.DTODiaDisponibilidad;
import com.mza_agrotours.backend.dtos.actividad.DTOTarifa;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.repositories.actividad.ActividadRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.*;

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

    private List<String> validarTarifas(DTOActividadAlta dto) {

        List<String> errores = new ArrayList<>();

        if (dto.getTarifas() == null ) {
            errores.add("Debes cargar al menos una tarifa para la actividad.");
        }

        Set<String> nombresTarifas = new HashSet<>();
        for (DTOTarifa tarifa : dto.getTarifas()) {
            String nombreNormalizado = tarifa.getNombre().toLowerCase();

            if (!nombresTarifas.add(nombreNormalizado)) {
                errores.add("No puedes registrar más de una tarifa con el mismo nombre ('" + tarifa.getNombre() + "').");
            }
        }

        long contadorTarifasBase = dto.getTarifas().stream().filter(DTOTarifa::isEsTarifaBase).count();
        if (contadorTarifasBase == 0) {
            errores.add("Es obligatorio marcar un rango etario como tarifa base.");
        } else if (contadorTarifasBase > 1) {
            errores.add("No puedes tener más de una tarifa base en la misma actividad.");
        }

        for (DTOTarifa tarifa : dto.getTarifas()) {
            if (tarifa.getEdadMinima() > tarifa.getEdadMaxima()) {
                errores.add("Error en la tarifa '" + tarifa.getNombre() + "': La edad mínima (" +
                        tarifa.getEdadMinima() + ") no puede ser mayor a la edad máxima (" +
                        tarifa.getEdadMaxima() + ").");
            }
        }

        //validación de solapamiento (error bloqueante)
        List<DTOTarifa> tarifasOrdenadas = new ArrayList<>(dto.getTarifas());
        tarifasOrdenadas.sort(Comparator.comparing(DTOTarifa::getEdadMinima));

        for (int i = 1; i < tarifasOrdenadas.size(); i++) {
            DTOTarifa anterior = tarifasOrdenadas.get(i - 1);
            DTOTarifa actual = tarifasOrdenadas.get(i);

            // Si la edad mínima del actual es MENOR o IGUAL a la edad máxima del anterior, se pisan
            if (actual.getEdadMinima() <= anterior.getEdadMaxima()) {
                errores.add("Solapamiento detectado: El rango '" + anterior.getNombre() +
                        "' tiene solapamiento de edades con el rango '" + actual.getNombre() + "'.");
            }
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
