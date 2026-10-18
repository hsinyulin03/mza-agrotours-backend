package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.config.MercadoPagoCheckoutProperties;
import com.mza_agrotours.backend.exceptions.pago.PasarelaPagoException;
import com.mza_agrotours.backend.services.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * El webhook solo confirma pagos con firma válida ({@code x-signature}: HMAC-SHA256 de
 * {@code id:<data.id>;request-id:<x-request-id>;ts:<ts>;}), y responde error únicamente cuando MP debe reintentar.
 */
class MercadoPagoWebhookControllerTest {

    private static final String SECRET = "clave-secreta-del-webhook";
    private static final String TS = "1742505638683";
    private static final String REQUEST_ID = "req-1";

    private final ReservaService reservaService = mock(ReservaService.class);
    private final MercadoPagoWebhookController controller = controllerCon(SECRET);

    private final UUID reservaId = UUID.randomUUID();

    private MercadoPagoWebhookController controllerCon(String secret) {
        return new MercadoPagoWebhookController(
                new MercadoPagoCheckoutProperties("http://front", secret == null ? null : "https://api/webhook", secret), reservaService);
    }

    private static String firma(String secret, String manifest) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "ts=" + TS + ",v1=" + HexFormat.of().formatHex(mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));
    }

    private static String firmaDe(String dataId) throws Exception {
        return firma(SECRET, "id:" + dataId + ";request-id:" + REQUEST_ID + ";ts:" + TS + ";");
    }

    @Test
    void conFirmaValidaConfirmaElPagoNotificado() throws Exception {
        var respuesta = controller.notificarPago(firmaDe("987"), REQUEST_ID, "987", "payment", reservaId.toString());

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        verify(reservaService).confirmarPagoNotificado(reservaId, "987");
    }

    @Test
    void elDataIdAlfanumericoSeValidaEnMinusculas() throws Exception {
        var respuesta = controller.notificarPago(firmaDe("abc123"), REQUEST_ID, "ABC123", "payment", reservaId.toString());

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    }

    @Test
    void conLaFirmaDeOtroPaymentRespondeUnauthorizedSinTocarLaReserva() throws Exception {
        var respuesta = controller.notificarPago(firmaDe("987"), REQUEST_ID, "988", "payment", reservaId.toString());

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        verifyNoInteractions(reservaService);
    }

    @Test
    void conUnaFirmaHechaConOtraClaveRespondeUnauthorized() throws Exception {
        String otraClave = firma("otra-clave", "id:987;request-id:" + REQUEST_ID + ";ts:" + TS + ";");

        assertEquals(HttpStatus.UNAUTHORIZED,
                controller.notificarPago(otraClave, REQUEST_ID, "987", "payment", reservaId.toString()).getStatusCode());
        verifyNoInteractions(reservaService);
    }

    @Test
    void sinFirmaOMalFormadaRespondeUnauthorized() {
        assertEquals(HttpStatus.UNAUTHORIZED, controller.notificarPago(null, REQUEST_ID, "987", "payment", reservaId.toString()).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, controller.notificarPago("ts=" + TS, REQUEST_ID, "987", "payment", reservaId.toString()).getStatusCode());
        verifyNoInteractions(reservaService);
    }

    @Test
    void sinClaveConfiguradaRespondeUnauthorized() throws Exception {
        var respuesta = controllerCon(null).notificarPago(firmaDe("987"), REQUEST_ID, "987", "payment", reservaId.toString());

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        verifyNoInteractions(reservaService);
    }

    @Test
    void otrosTiposDeNotificacionSeIgnoran() throws Exception {
        var respuesta = controller.notificarPago(firmaDe("555"), REQUEST_ID, "555", "merchant_order", reservaId.toString());

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        verifyNoInteractions(reservaService);
    }

    @Test
    void sinUnaReservaValidaSeIgnoraParaQueMpNoReintente() throws Exception {
        assertEquals(HttpStatus.OK, controller.notificarPago(firmaDe("987"), REQUEST_ID, "987", "payment", null).getStatusCode());
        assertEquals(HttpStatus.OK, controller.notificarPago(firmaDe("987"), REQUEST_ID, "987", "payment", "basura").getStatusCode());
        verifyNoInteractions(reservaService);
    }

    @Test
    void unErrorDeLaPasarelaRespondeErrorParaQueMpReintente() throws Exception {
        doThrow(new PasarelaPagoException("timeout", null)).when(reservaService).confirmarPagoNotificado(reservaId, "987");

        var respuesta = controller.notificarPago(firmaDe("987"), REQUEST_ID, "987", "payment", reservaId.toString());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
    }
}
