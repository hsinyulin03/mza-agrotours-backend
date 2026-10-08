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
    private CuentaMercadoPagoRepository cuentaMercadoPagoRepository;
    private MercadoPagoOAuthClient oAuthClient;
    private CuentaMercadoPagoService service;

    private Establecimiento establecimiento;
    private Productor titular;

    @BeforeEach
    void setUp() {
        establecimientoRepository = mock(EstablecimientoRepository.class);
        cuentaMercadoPagoRepository = mock(CuentaMercadoPagoRepository.class);
        oAuthClient = mock(MercadoPagoOAuthClient.class);
        service = new CuentaMercadoPagoService(
                establecimientoRepository, cuentaMercadoPagoRepository, oAuthClient, cifrador);

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

    // ---------- renovarTokens ----------

    private CuentaMercadoPago cuentaVinculada() {
        CuentaMercadoPago cuenta = new CuentaMercadoPago();
        cuenta.setId(UUID.randomUUID());
        cuenta.setVinculadaPor(titular);
        cuenta.setMpUserId(123456L);
        cuenta.setAccessToken("APP_USR-viejo");
        cuenta.setRefreshToken("TG-refresh-viejo");
        cuenta.setFechaHoraExpiracionToken(LocalDateTime.now().plusDays(10));
        establecimiento.vincularCuentaMercadoPago(cuenta, LocalDateTime.now().minusDays(170));
        when(cuentaMercadoPagoRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));
        return cuenta;
    }

    @Test
    void renuevaLosTokensConElRefreshTokenVigente() {
        CuentaMercadoPago cuenta = cuentaVinculada();
        when(oAuthClient.renovarToken("TG-refresh-viejo")).thenReturn(
                new MercadoPagoOAuthTokenResponse("APP_USR-nuevo", "TG-refresh-nuevo", null, 123456L, 15552000L, false));

        service.renovarTokens(cuenta.getId());

        assertEquals("APP_USR-nuevo", cuenta.getAccessToken());
        assertEquals("TG-refresh-nuevo", cuenta.getRefreshToken());
        assertTrue(cuenta.getFechaHoraExpiracionToken().isAfter(LocalDateTime.now().plusDays(179)));
        assertNotNull(cuenta.getFechaHoraUltimaRenovacion());
        assertTrue(cuenta.isVigente());
        verify(cuentaMercadoPagoRepository).save(cuenta);
    }

    @Test
    void conservaLaPublicKeySiLaRenovacionNoLaDevuelve() {
        CuentaMercadoPago cuenta = cuentaVinculada();
        cuenta.setPublicKey("APP_USR-public-original");
        when(oAuthClient.renovarToken(anyString())).thenReturn(
                new MercadoPagoOAuthTokenResponse("APP_USR-nuevo", "TG-refresh-nuevo", null, 123456L, 15552000L, false));

        service.renovarTokens(cuenta.getId());

        assertEquals("APP_USR-public-original", cuenta.getPublicKey());
    }

    @Test
    void noRenuevaUnaCuentaDadaDeBaja() {
        CuentaMercadoPago cuenta = cuentaVinculada();
        cuenta.setFechaHoraBaja(LocalDateTime.now());

        service.renovarTokens(cuenta.getId());

        verify(oAuthClient, never()).renovarToken(any());
        verify(cuentaMercadoPagoRepository, never()).save(any());
    }

    @Test
    void noRenuevaLaCuentaDeUnEstablecimientoDadoDeBaja() {
        CuentaMercadoPago cuenta = cuentaVinculada();
        establecimiento.setFechaHoraBaja(LocalDateTime.now());

        service.renovarTokens(cuenta.getId());

        verify(oAuthClient, never()).renovarToken(any());
        verify(cuentaMercadoPagoRepository, never()).save(any());
    }

    @Test
    void siMpRechazaLaRenovacionLaCuentaQuedaIgual() {
        CuentaMercadoPago cuenta = cuentaVinculada();
        when(oAuthClient.renovarToken(anyString())).thenThrow(new MercadoPagoOAuthException("invalid_grant"));

        assertThrows(MercadoPagoOAuthException.class, () -> service.renovarTokens(cuenta.getId()));

        assertEquals("APP_USR-viejo", cuenta.getAccessToken());
        assertEquals("TG-refresh-viejo", cuenta.getRefreshToken());
        verify(cuentaMercadoPagoRepository, never()).save(any());
    }
}