package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.calificacion.*;
import com.mza_agrotours.backend.entities.Calificacion;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.CalificacionError;
import com.mza_agrotours.backend.exceptions.reservas.ReservaNotFoundException;
import com.mza_agrotours.backend.repositories.CalificacionRepository;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CalificacionService {
    private static final int CANTIDAD_RESENIAS_RECIENTES = 3;
    private final ReservaRepository reservaRepository;
    private final CalificacionRepository calificacionRepository;
    private final ActividadRepository actividadRepository;

    public CalificacionService(ReservaRepository reservaRepository, CalificacionRepository calificacionRepository, ActividadRepository actividadRepository) {
        this.reservaRepository = reservaRepository;
        this.calificacionRepository = calificacionRepository;
        this.actividadRepository = actividadRepository;
    }

    @Transactional
    public CalificacionResponseDTO calificarReserva(UUID reservaId, CalificacionDTO dto, String emailUsuario) {

        // Obtenemos la reserva y verificamos que sea del usuario
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(ReservaNotFoundException::new);

        if (!reserva.getVisitante().getUsuario().getEmail().equals(emailUsuario))
            throw new ReservaNotFoundException();

        // Solo se valoran reservas finalizadas
        if (reserva.getEstadoActual().getEstadoReserva().getNombre() != EstadoReservaNombre.FINALIZADA)
            throw new AppException(CalificacionError.RESERVA_NO_FINALIZADA);

        // No debe haberla valorado previamente
        if (reserva.getCalificacion() != null)
            throw new AppException(CalificacionError.YA_CALIFICADA);

        Calificacion calificacion = new Calificacion(dto.getPuntaje(), dto.getResenia(), LocalDateTime.now());

        Actividad actividad = reserva.getActividad();
        reserva.setCalificacion(calificacion);
        actividad.getCalificaciones().add(calificacion);
        calificacionRepository.save(calificacion);

        // Actualizamos el promedio de la actividad.
        int sumaPuntajes = 0;
        for (Calificacion c : actividad.getCalificaciones()) {
            sumaPuntajes += c.getPuntaje();
        }
        float promedio = (float) sumaPuntajes / actividad.getCalificaciones().size();
        actividad.setCalificacionPromedio(Math.round(promedio * 10) / 10f); //redondea a un decimal ej: 4.1666->4.2

        return new CalificacionResponseDTO(calificacion.getId(), reserva.getId(), calificacion.getPuntaje(),
                                           calificacion.getResenia(), calificacion.getFechaHoraCalificacion());

    }

    //US-ACT-02: Reseñas de la actividad - resumen (promedio, total y distribución por estrellas)
    @Transactional(readOnly = true)
    public ResumenReseniasDTO obtenerResumenResenias(UUID idActividad) {
        // Buscar la cantidad de reseñas por puntaje de una actividad
        List<DistribucionPuntajeDTO> conteos = calificacionRepository.contarPorPuntaje(idActividad);

        Map<Integer, Long> cantidadPorPuntaje = new HashMap<>();
        long total = 0;
        long sumaPuntajes = 0;
        for (DistribucionPuntajeDTO conteo : conteos) {
            cantidadPorPuntaje.put(conteo.getPuntaje(), conteo.getCantidad());
            total += conteo.getCantidad();
            sumaPuntajes += conteo.getPuntaje() * conteo.getCantidad(); //para poder calcular el promedio al final
        }

        // Siempre devolvemos las 5 barras, aunque alguna tenga 0
        List<DistribucionPuntajeDTO> distribucion = new ArrayList<>();
        for (int puntaje = 5; puntaje >= 1; puntaje--) {
            long cantidad = cantidadPorPuntaje.getOrDefault(puntaje, 0L);
            DistribucionPuntajeDTO barra = new DistribucionPuntajeDTO(puntaje, cantidad);
            barra.setPorcentaje(total == 0 ? 0 : (int) Math.round(cantidad * 100.0 / total)); //redondea el porcentaje a entero ej: 94.117->94
            distribucion.add(barra);
        }

        ResumenReseniasDTO dto = new ResumenReseniasDTO();
        dto.setPromedio(total == 0 ? null : Math.round((double) sumaPuntajes / total * 10) / 10.0); //redondea el promedio general a un decimal ej: 4.166666->4.2
        dto.setTotalResenias(total);
        dto.setDistribucion(distribucion);
        return dto;
    }

    //US-ACT-02: Reseñas de la actividad - las 3 más recientes
    @Transactional(readOnly = true)
    public List<ReseniaCardDTO> obtenerReseniasRecientes(UUID idActividad) {
        Pageable recientes = PageRequest.of(0, CANTIDAD_RESENIAS_RECIENTES,
                Sort.by(Sort.Direction.DESC, "fechaHoraCalificacion", "id"));
        return calificacionRepository.findReseniasByActividad(idActividad, null, recientes).getContent();
    }

}
