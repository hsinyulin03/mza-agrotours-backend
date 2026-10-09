package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService;
import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService.ResultadoCallback;
import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService.ResultadoVinculacion;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * El callback de OAuth siempre redirige al front con el resultado, sin exponer errores ni datos de MP.
 */
class MercadoPagoOAuthCallbackControllerTest {

    private static final String FRONT = "http://localhost:5173/mercadopago/vinculacion";

    private final CuentaMercadoPagoService service = mock(CuentaMercadoPagoService.class);
    private final MercadoPagoOAuthCallbackController controller = new MercadoPagoOAuthCallbackController(service, FRONT);

    private static Map<String, String> parametrosDe(ResponseEntity<Void> respuesta) {
        assertEquals(HttpStatus.FOUND, respuesta.getStatusCode());
        String destino = respuesta.getHeaders().getLocation().toString();
        assertTrue(destino.startsWith(FRONT + "?"), destino);
        return UriComponentsBuilder.fromUriString(destino).build().getQueryParams().toSingleValueMap();
    }

    @Test
    void redirigeAlFrontConElResultadoYElEstablecimiento() {
        UUID establecimientoId = UUID.randomUUID();
        when(service.procesarCallback("TG-code", "state"))
                .thenReturn(new ResultadoCallback(ResultadoVinculacion.OK, establecimientoId));

        Map<String, String> params = parametrosDe(controller.callback("TG-code", "state"));

        assertEquals("ok", params.get("resultado"));
        assertEquals(establecimientoId.toString(), params.get("establecimientoId"));
    }

    @Test
    void sinEstablecimientoNoLoIncluyeEnLaRedireccion() {
        when(service.procesarCallback(any(), any())).thenReturn(new ResultadoCallback(ResultadoVinculacion.ERROR, null));

        Map<String, String> params = parametrosDe(controller.callback("TG-code", "basura"));

        assertEquals("error", params.get("resultado"));
        assertFalse(params.containsKey("establecimientoId"));
    }

    @Test
    void cancelacionRedirigeComoCancelada() {
        UUID establecimientoId = UUID.randomUUID();
        when(service.procesarCallback(null, "state"))
                .thenReturn(new ResultadoCallback(ResultadoVinculacion.CANCELADA, establecimientoId));

        assertEquals("cancelada", parametrosDe(controller.callback(null, "state")).get("resultado"));
    }

    @Test
    void unErrorInesperadoRedirigeComoErrorSinExponerlo() {
        when(service.procesarCallback(any(), any())).thenThrow(new IllegalStateException("detalle interno"));

        ResponseEntity<Void> respuesta = controller.callback("TG-code", "state");
        Map<String, String> params = parametrosDe(respuesta);

        assertEquals("error", params.get("resultado"));
        assertFalse(params.containsKey("establecimientoId"));
        assertFalse(respuesta.getHeaders().getLocation().toString().contains("detalle"));
    }
}
