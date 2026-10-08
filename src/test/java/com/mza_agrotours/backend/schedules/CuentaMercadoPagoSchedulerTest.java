package com.mza_agrotours.backend.schedules;

import com.mza_agrotours.backend.exceptions.pago.MercadoPagoOAuthException;
import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CuentaMercadoPagoSchedulerTest {

    private final CuentaMercadoPagoService service = mock(CuentaMercadoPagoService.class);
    private final CuentaMercadoPagoScheduler scheduler = new CuentaMercadoPagoScheduler(service);

    @Test
    void unaCuentaQueFallaNoFrenaLaRenovacionDeLasDemas() {
        UUID revocada = UUID.randomUUID();
        UUID primera = UUID.randomUUID();
        UUID ultima = UUID.randomUUID();
        when(service.getCuentasARenovar()).thenReturn(List.of(primera, revocada, ultima));
        doThrow(new MercadoPagoOAuthException("invalid_grant")).when(service).renovarTokens(revocada);

        assertDoesNotThrow(scheduler::renovarTokensPorVencer);

        verify(service).renovarTokens(primera);
        verify(service).renovarTokens(revocada);
        verify(service).renovarTokens(ultima);
    }

    @Test
    void sinCuentasPorVencerNoRenuevaNada() {
        when(service.getCuentasARenovar()).thenReturn(List.of());

        scheduler.renovarTokensPorVencer();

        verify(service, never()).renovarTokens(any());
    }
}
