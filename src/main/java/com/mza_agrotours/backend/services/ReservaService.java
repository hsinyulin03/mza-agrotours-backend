package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDTO;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.exceptions.UsuarioDeactivatedException;
import com.mza_agrotours.backend.exceptions.reservas.ReservaNotFoundException;
import com.mza_agrotours.backend.mappers.reserva.ReservaMapper;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;

    public ReservaService(ReservaRepository reservaRepository, ReservaMapper reservaMapper) {
        this.reservaRepository = reservaRepository;
        this.reservaMapper = reservaMapper;
    }

    /**
     * Devuelve todos los datos necesarios para que un visitante consulte una reserva específica<p></p>
     *
     * @param id UUID de la Reserva
     * @param firebaseUID UID del usuario en firebase
     * @return <code>ConsultarReservaDTO</code> con los datos de una reserva
     * @throws ReservaNotFoundException si la reserva no existe o no pertenece al usuario autenticado
     * @throws UsuarioDeactivatedException si el usuario dueño de la reserva está dado de baja
     */
    @Transactional
    public ConsultarReservaDTO getConsultarReserva(UUID id, String firebaseUID){
        // Obtenemos la reserva, si no existe error.
        Reserva reserva = reservaRepository.findById(id).orElseThrow(ReservaNotFoundException::new);

        // Verificar que la reserva sea del usuario. Si no lo es, NOT FOUND para evitar dar información a no autorizados
         if (!reserva.getVisitante().getUsuario().getFirebaseUID().equals(firebaseUID))
             throw new ReservaNotFoundException();

         // Verificar que el usuario esté de alta
        if (reserva.getVisitante().getUsuario().getFechaHoraBaja() != null)
            throw new UsuarioDeactivatedException();

        // Armamos el DTO
        return reservaMapper.reservaToConsultarReservaDTO(reserva);
    }
}
