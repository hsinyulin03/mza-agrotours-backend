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

    @Scheduled(fixedDelay = 60L, timeUnit = TimeUnit.SECONDS)
    public void checkReservasPagadas(){
        reservaService.pagarReservas();
    }

    // El recordatorio se envía a las reservas cuyo día empieza en las próximas 24 h
    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.HOURS)
    public void checkRecordatoriosReservas() {
        reservaService.enviarRecordatoriosPendientes();
    }
}
