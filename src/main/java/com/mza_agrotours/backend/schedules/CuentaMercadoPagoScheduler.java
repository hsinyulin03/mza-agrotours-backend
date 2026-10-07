package com.mza_agrotours.backend.schedules;

import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CuentaMercadoPagoScheduler {
    private final CuentaMercadoPagoService cuentaMercadoPagoService;

    public CuentaMercadoPagoScheduler(CuentaMercadoPagoService cuentaMercadoPagoService) {
        this.cuentaMercadoPagoService = cuentaMercadoPagoService;
    }

    // Los tokens de MP duran 180 días y se renuevan con 30 de anticipación:
    // revisar una vez por día alcanza y deja margen para reintentar si MP falla.
    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.DAYS)
    public void renovarTokensPorVencer() {
        List<UUID> cuentas = cuentaMercadoPagoService.getCuentasARenovar();
        List<UUID> fallidas = new ArrayList<>();

        for (UUID cuentaId : cuentas) {
            try {
                cuentaMercadoPagoService.renovarTokens(cuentaId);
            } catch (Exception e) {
                // Si el productor revocó el acceso desde MP, falla hasta que vuelva a vincular
                log.warn("No se pudo renovar el token de la cuenta de MP {}: {}", cuentaId, e.getMessage());
                fallidas.add(cuentaId);
            }
        }

        if (!cuentas.isEmpty())
            log.info("Se renovaron {}/{} tokens de MP. Fallidas: {}", cuentas.size() - fallidas.size(), cuentas.size(), fallidas);
    }
}