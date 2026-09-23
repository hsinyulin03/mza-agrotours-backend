package com.mza_agrotours.backend.schedules;

import com.mza_agrotours.backend.services.ClimaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PronosticoClimaScheduler {
    private final ClimaService climaService;

    public PronosticoClimaScheduler(ClimaService climaService) {
        this.climaService = climaService;
    }

    @Scheduled(cron = "0 0 5 * * *", zone = ClimaService.ZONA_ID)
    public void actualizarPronosticos() {
        this.climaService.actualizarPronosticos();
    }
}
