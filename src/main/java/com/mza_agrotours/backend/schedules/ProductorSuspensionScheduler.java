package com.mza_agrotours.backend.schedules;

import com.mza_agrotours.backend.services.ProductorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ProductorSuspensionScheduler {
    private final ProductorService productorService;

    public ProductorSuspensionScheduler(ProductorService productorService) {
        this.productorService = productorService;
    }

    // Las suspensiones se miden en dias, asi que no hace falta resolucion fina:
    // con revisar una vez por hora alcanza.
    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.HOURS)
    public void checkSuspensionesVencidas() {
        productorService.levantarSuspensionesVencidas();
    }
}
