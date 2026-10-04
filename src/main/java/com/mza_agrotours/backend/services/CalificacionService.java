package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.calificacion.CalificacionDTO;
import com.mza_agrotours.backend.dtos.calificacion.CalificacionResponseDTO;
import com.mza_agrotours.backend.entities.Calificacion;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.CalificacionError;
import com.mza_agrotours.backend.exceptions.reservas.ReservaNotFoundException;
import com.mza_agrotours.backend.repositories.CalificacionRepository;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CalificacionService {

    private final ReservaRepository reservaRepository;
    private final CalificacionRepository calificacionRepository;

    public CalificacionService(ReservaRepository reservaRepository, CalificacionRepository calificacionRepository) {
        this.reservaRepository = reservaRepository;
        this.calificacionRepository = calificacionRepository;
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

        reserva.setCalificacion(calificacion);
        reserva.getActividad().getCalificaciones().add(calificacion);
        calificacionRepository.save(calificacion);

        return new CalificacionResponseDTO(calificacion.getId(), reserva.getId() , calificacion.getPuntaje(), calificacion.getResenia(), calificacion.getFechaHoraCalificacion());

    }

}
