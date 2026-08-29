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

    @Scheduled(fixedDelay = 60L, timeUnit = TimeUnit.SECONDS)
    public void checkReservasExpiradas(){
        reservaService.expirarReservas();
    }
}
