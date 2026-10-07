package com.mza_agrotours.backend.clients.mercadopago;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MercadoPagoOAuthClientTest {

    @Test
    void construyeLaUrlDeAutorizacionConLosParametrosDeMp() {
        MercadoPagoOAuthClient client = new MercadoPagoOAuthClient(
                RestClient.create(),
                "https://auth.mercadopago.com.ar/authorization",
                "1234567890",
                "secreto",
                "https://abc-8080.brs.devtunnels.ms/mercadopago/oauth/callback",
                false);

        String url = client.construirUrlAutorizacion("state_cifrado-123");

        assertTrue(url.startsWith("https://auth.mercadopago.com.ar/authorization?"));
        Map<String, String> params = UriComponentsBuilder.fromUriString(url).build().getQueryParams().toSingleValueMap();
        assertEquals("1234567890", params.get("client_id"));
        assertEquals("code", params.get("response_type"));
        assertEquals("mp", params.get("platform_id"));
        assertEquals("state_cifrado-123", params.get("state"));
        assertEquals("https://abc-8080.brs.devtunnels.ms/mercadopago/oauth/callback",
                java.net.URLDecoder.decode(params.get("redirect_uri"), java.nio.charset.StandardCharsets.UTF_8));
        assertTrue(!url.contains("secreto"));
    }
}