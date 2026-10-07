package com.mza_agrotours.backend.services.pago;

import com.mza_agrotours.backend.clients.mercadopago.MercadoPagoOAuthClient;
import com.mza_agrotours.backend.clients.mercadopago.MercadoPagoOAuthTokenResponse;
import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.exceptions.pago.MercadoPagoOAuthException;
import com.mza_agrotours.backend.repositories.CuentaMercadoPagoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.security.cifrado.Cifrador;
import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService.ResultadoCallback;
import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService.ResultadoVinculacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CuentaMercadoPagoServiceTest {

    private final Cifrador cifrador = new Cifrador(Base64.getEncoder().encodeToString(new byte[32]));
    private EstablecimientoRepository establecimientoRepository;
    private MercadoPagoOAuthClient oAuthClient;
    private CuentaMercadoPagoService service;

    private Establecimiento establecimiento;
    private Productor titular;

    @BeforeEach
    void setUp() {
        establecimientoRepository = mock(EstablecimientoRepository.class);
        oAuthClient = mock(MercadoPagoOAuthClient.class);
        service = new CuentaMercadoPagoService(
                establecimientoRepository, mock(CuentaMercadoPagoRepository.class), oAuthClient, cifrador);

        titular = new Productor();
        titular.setId(UUID.randomUUID());
        establecimiento = new Establecimiento();
        establecimiento.setId(UUID.randomUUID());
        establecimiento.setTitular(titular);

        when(establecimientoRepository.findById(establecimiento.getId())).thenReturn(Optional.of(establecimiento));
        when(oAuthClient.construirUrlAutorizacion(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }

    /** construirUrlAutorizacion está mockeado para devolver el state tal cual. */
    private String stateValido() {
        return service.generarUrlVinculacion(establecimiento.getId());
    }

    private static MercadoPagoOAuthTokenResponse tokens(String accessToken) {
        return new MercadoPagoOAuthTokenResponse(accessToken, "TG-refresh", "APP_USR-public", 123456L, 15552000L, false);
    }

    @Test
    void vinculaLaCuentaConUnStateValido() {
        when(oAuthClient.canjearCodigo("TG-code")).thenReturn(tokens("APP_USR-access"));

        ResultadoCallback resultado = service.procesarCallback("TG-code", stateValido());

        assertEquals(ResultadoVinculacion.OK, resultado.resultado());
        assertEquals(establecimiento.getId(), resultado.establecimientoId());

        CuentaMercadoPago cuenta = establecimiento.getCuentaMercadoPagoVigente().orElseThrow();
        assertEquals("APP_USR-access", cuenta.getAccessToken());
        assertEquals(123456L, cuenta.getMpUserId());
        assertSame(titular, cuenta.getVinculadaPor());
        assertTrue(cuenta.getFechaHoraExpiracionToken().isAfter(LocalDateTime.now().plusDays(179)));
        verify(establecimientoRepository).save(establecimiento);
    }

    @Test
    void revincularDaDeBajaLaCuentaAnterior() {
        when(oAuthClient.canjearCodigo(anyString())).thenReturn(tokens("APP_USR-1"), tokens("APP_USR-2"));

        service.procesarCallback("TG-code-1", stateValido());
        service.procesarCallback("TG-code-2", stateValido());

        assertEquals(2, establecimiento.getCuentasMercadoPago().size());
        assertEquals(1, establecimiento.getCuentasMercadoPago().stream().filter(CuentaMercadoPago::isVigente).count());
        assertEquals("APP_USR-2", establecimiento.getCuentaMercadoPagoVigente().orElseThrow().getAccessToken());
    }

    @Test
    void rechazaStateFalsificadoAlteradoOVacio() {
        String falsificado = new Cifrador(Base64.getEncoder().encodeToString(new byte[32]).replace('A', 'B'))
                .cifrar(establecimiento.getId() + "|" + titular.getId() + "|" + Instant.now().plusSeconds(600).getEpochSecond());

        for (String state : new String[]{falsificado, "basura", "", null}) {
            ResultadoCallback resultado = service.procesarCallback("TG-code", state);
            assertEquals(ResultadoVinculacion.ERROR, resultado.resultado());
            assertNull(resultado.establecimientoId());
        }
        verify(oAuthClient, never()).canjearCodigo(any());
    }

    @Test
    void rechazaStateVencido() {
        String vencido = cifrador.cifrar(
                establecimiento.getId() + "|" + titular.getId() + "|" + Instant.now().minusSeconds(1).getEpochSecond());

        assertEquals(ResultadoVinculacion.ERROR, service.procesarCallback("TG-code", vencido).resultado());
        verify(oAuthClient, never()).canjearCodigo(any());
    }

    @Test
    void rechazaSiQuienInicioYaNoEsTitular() {
        String state = stateValido();
        Productor nuevoTitular = new Productor();
        nuevoTitular.setId(UUID.randomUUID());
        establecimiento.setTitular(nuevoTitular);

        assertEquals(ResultadoVinculacion.ERROR, service.procesarCallback("TG-code", state).resultado());
        verify(oAuthClient, never()).canjearCodigo(any());
        assertTrue(establecimiento.getCuentaMercadoPagoVigente().isEmpty());
    }

    @Test
    void sinCodeEsCancelada() {
        ResultadoCallback resultado = service.procesarCallback(null, stateValido());

        assertEquals(ResultadoVinculacion.CANCELADA, resultado.resultado());
        assertEquals(establecimiento.getId(), resultado.establecimientoId());
        verify(oAuthClient, never()).canjearCodigo(any());
    }

    @Test
    void errorDeMpDevuelveErrorSinVincular() {
        when(oAuthClient.canjearCodigo("TG-code")).thenThrow(new MercadoPagoOAuthException("invalid_grant"));

        assertEquals(ResultadoVinculacion.ERROR, service.procesarCallback("TG-code", stateValido()).resultado());
        assertTrue(establecimiento.getCuentaMercadoPagoVigente().isEmpty());
        verify(establecimientoRepository, never()).save(any());
    }
}