package com.mza_agrotours.backend.clients.mercadopago;

import com.mza_agrotours.backend.exceptions.pago.MercadoPagoOAuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class MercadoPagoOAuthClientTest {

    private static final String BASE_URL = "https://api.mercadopago.com";
    private static final String REDIRECT_URI = "https://abc-8080.brs.devtunnels.ms/mercadopago/oauth/callback";
    private static final String RESPUESTA_OK = """
            {"access_token":"APP_USR-access","refresh_token":"TG-refresh","public_key":"APP_USR-public",
             "user_id":123456,"expires_in":15552000,"live_mode":false,"token_type":"Bearer","scope":"offline_access"}
            """;

    private MockRestServiceServer servidor;
    private MercadoPagoOAuthClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        servidor = MockRestServiceServer.bindTo(builder).build();
        client = new MercadoPagoOAuthClient(builder.build(),
                "https://auth.mercadopago.com.ar/authorization", "1234567890", "secreto", REDIRECT_URI, true);
    }

    @Test
    void construyeLaUrlDeAutorizacionConLosParametrosDeMp() {
        String url = client.construirUrlAutorizacion("state_cifrado-123");

        assertTrue(url.startsWith("https://auth.mercadopago.com.ar/authorization?"));
        Map<String, String> params = UriComponentsBuilder.fromUriString(url).build().getQueryParams().toSingleValueMap();
        assertEquals("1234567890", params.get("client_id"));
        assertEquals("code", params.get("response_type"));
        assertEquals("mp", params.get("platform_id"));
        assertEquals("state_cifrado-123", params.get("state"));
        assertEquals(REDIRECT_URI,
                java.net.URLDecoder.decode(params.get("redirect_uri"), java.nio.charset.StandardCharsets.UTF_8));
        assertFalse(url.contains("secreto"));
    }

    @Test
    void canjeaElCodigoConLasCredencialesDeLaAplicacion() {
        servidor.expect(requestTo(BASE_URL + "/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.grant_type").value("authorization_code"))
                .andExpect(jsonPath("$.client_id").value("1234567890"))
                .andExpect(jsonPath("$.client_secret").value("secreto"))
                .andExpect(jsonPath("$.code").value("TG-code"))
                .andExpect(jsonPath("$.redirect_uri").value(REDIRECT_URI))
                .andExpect(jsonPath("$.test_token").value(true))
                .andRespond(withSuccess(RESPUESTA_OK, MediaType.APPLICATION_JSON));

        MercadoPagoOAuthTokenResponse tokens = client.canjearCodigo("TG-code");

        assertEquals("APP_USR-access", tokens.accessToken());
        assertEquals("TG-refresh", tokens.refreshToken());
        assertEquals("APP_USR-public", tokens.publicKey());
        assertEquals(123456L, tokens.userId());
        assertEquals(15552000L, tokens.expiresIn());
        servidor.verify();
    }

    @Test
    void renuevaConElRefreshToken() {
        servidor.expect(requestTo(BASE_URL + "/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.grant_type").value("refresh_token"))
                .andExpect(jsonPath("$.refresh_token").value("TG-refresh"))
                .andExpect(jsonPath("$.client_id").value("1234567890"))
                .andExpect(jsonPath("$.code").doesNotExist())
                .andRespond(withSuccess(RESPUESTA_OK, MediaType.APPLICATION_JSON));

        assertEquals("APP_USR-access", client.renovarToken("TG-refresh").accessToken());
        servidor.verify();
    }

    @Test
    void unRechazoDeMpIncluyeElMotivoEnLaExcepcion() {
        servidor.expect(requestTo(BASE_URL + "/oauth/token"))
                .andRespond(withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_grant\",\"message\":\"code expired\"}"));

        MercadoPagoOAuthException e = assertThrows(MercadoPagoOAuthException.class, () -> client.canjearCodigo("TG-code"));
        assertTrue(e.getMessage().contains("invalid_grant"));
        assertFalse(e.getMessage().contains("secreto"));
    }

    @Test
    void unErrorDeRedSeInformaComoMercadoPagoOAuthException() {
        servidor.expect(requestTo(BASE_URL + "/oauth/token"))
                .andRespond(withException(new IOException("connection reset")));

        assertThrows(MercadoPagoOAuthException.class, () -> client.renovarToken("TG-refresh"));
    }

    @Test
    void rechazaUnaRespuestaSinUserId() {
        servidor.expect(requestTo(BASE_URL + "/oauth/token"))
                .andRespond(withSuccess("""
                        {"access_token":"APP_USR-access","refresh_token":"TG-refresh","expires_in":15552000}
                        """, MediaType.APPLICATION_JSON));

        assertThrows(MercadoPagoOAuthException.class, () -> client.canjearCodigo("TG-code"));
    }
}
