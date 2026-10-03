package com.mza_agrotours.backend.schedules;

import com.mza_agrotours.backend.services.ActividadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ActividadDiaFinalizadaScheduler {
    private final ActividadService actividadService;

    public ActividadDiaFinalizadaScheduler(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    // Los días terminan a horarios variados, se revisa cada 1 hora
    @Scheduled(fixedDelay = 60L, timeUnit = TimeUnit.MINUTES)
    public void checkDiasTerminados() {
        int finalizados = actividadService.finalizarDiasTerminados();
        if (finalizados > 0) {
            log.info("Se finalizaron {} días de actividad que ya habían ocurrido y estaban activos o reprogramados.", finalizados);
        }
    }
}