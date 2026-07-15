package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDTO;
import com.mza_agrotours.backend.entities.reservas.Reserva;
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

    @Transactional
    public ConsultarReservaDTO getConsultarReserva(UUID id){
        // TODO - Habría que checkear al usuario antes de hacer las cosas de esta función

        // Obtenemos la reserva
        Reserva reserva = reservaRepository.getReferenceById(id);

        // Hacemos los chequeos - la reserva exista y es del usuario

        // Armamos el DTO
        ConsultarReservaDTO reservaDto = reservaMapper.reservaToConsultarReservaDTO(reserva);
        return reservaDto;
    }
}
