package com.mza_agrotours.backend.config.outbox;

import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.services.ReservaService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReembolsarReservaOutboxStrategy implements OutboxStrategy{
    private final ReservaService reservaService;
    private final ReservaRepository reservaRepository;

    public ReembolsarReservaOutboxStrategy(
            ReservaService reservaService,
            ReservaRepository reservaRepository) {
        this.reservaService = reservaService;
        this.reservaRepository = reservaRepository;
    }

    @Override
    public void resolver(Outbox outbox) throws Exception {
        Reserva reserva = this.reservaRepository.findById(UUID.fromString(outbox.getEntidadId()))
                .orElse(null);

        if (reserva == null) {
            return;
        }

        this.reservaService.reembolsarReserva(reserva);
    }
}
