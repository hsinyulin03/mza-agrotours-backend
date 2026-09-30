package com.mza_agrotours.backend.schedules;

import com.mza_agrotours.backend.services.ReservaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ReservaEstadosScheduler {
    private final ReservaService reservaService;

    public ReservaEstadosScheduler(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @Scheduled(initialDelay = 10L, fixedDelay = 60L, timeUnit = TimeUnit.SECONDS)
    public void checkReservasExpiradas(){
        reservaService.expirarReservas();
    }

    @Scheduled(initialDelay = 10L, fixedDelay = 60L, timeUnit = TimeUnit.SECONDS)
    public void checkReservasPagadas(){
        reservaService.pagarReservas();
    }

    @Scheduled(initialDelay = 10L, fixedDelay = 60L, timeUnit = TimeUnit.SECONDS)
    public void checkReembolsosConfirmados(){
        reservaService.confirmarReembolsos();
    }
}
