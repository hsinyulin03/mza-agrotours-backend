package com.mza_agrotours.backend.clients.mercadopago;

import com.mza_agrotours.backend.exceptions.pago.MercadoPagoOAuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente del flujo OAuth (authorization code) de Mercado Pago, con el que los establecimientos
 * vinculan su cuenta de MP a la aplicación de Agrotours (marketplace).
 * <p></p>
 * No se usa el OauthClient del SDK (2.1.7) porque envía el access token como client_secret,
 * no envía el client_id y descarta el user_id de la respuesta.
 */
@Component
public class MercadoPagoOAuthClient {
    private static final String RECURSO_TOKEN = "/oauth/token";

    private final RestClient mercadoPagoRestClient;
    private final String authUrl;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final boolean testToken;

    public MercadoPagoOAuthClient(RestClient mercadoPagoRestClient,
                                  @Value("${mercadopago.oauth.auth-url}") String authUrl,
                                  @Value("${mercadopago.oauth.client-id}") String clientId,
                                  @Value("${mercadopago.oauth.client-secret}") String clientSecret,
                                  @Value("${mercadopago.oauth.redirect-uri}") String redirectUri,
                                  @Value("${mercadopago.oauth.test-token:false}") boolean testToken) {
        this.mercadoPagoRestClient = mercadoPagoRestClient;
        this.authUrl = authUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.testToken = testToken;
    }

    /**
     * URL de la página de Mercado Pago donde el vendedor inicia sesión y autoriza a la aplicación.
     */
    public String construirUrlAutorizacion(String state) {
        return UriComponentsBuilder.fromUriString(authUrl)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("platform_id", "mp")
                .queryParam("state", state)
                .queryParam("redirect_uri", redirectUri)
                .encode()
                .toUriString();
    }

    /**
     * Canjea el código recibido en el callback por los tokens del vendedor.
     */
    public MercadoPagoOAuthTokenResponse canjearCodigo(String code) {
        Map<String, Object> body = cuerpoBase("authorization_code");
        body.put("code", code);
        body.put("redirect_uri", redirectUri);
        body.put("test_token", testToken);
        return solicitarToken(body);
    }

    /**
     * Obtiene un nuevo par de tokens a partir del refresh token vigente del vendedor.
     */
    public MercadoPagoOAuthTokenResponse renovarToken(String refreshToken) {
        Map<String, Object> body = cuerpoBase("refresh_token");
        body.put("refresh_token", refreshToken);
        return solicitarToken(body);
    }

    private Map<String, Object> cuerpoBase(String grantType) {
        Map<String, Object> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("grant_type", grantType);
        return body;
    }

    private MercadoPagoOAuthTokenResponse solicitarToken(Map<String, Object> body) {
        MercadoPagoOAuthTokenResponse respuesta;
        try {
            respuesta = this.mercadoPagoRestClient.post()
                    .uri(RECURSO_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(MercadoPagoOAuthTokenResponse.class);
        } catch (RestClientResponseException e) {
            // El cuerpo de error de MP no incluye credenciales, sirve para diagnosticar (code expirado, redirect_uri distinta, etc.)
            throw new MercadoPagoOAuthException(
                    "MP rechazó la solicitud de token (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new MercadoPagoOAuthException("Falló la comunicación con MP al solicitar el token", e);
        }

        if (respuesta == null || respuesta.accessToken() == null || respuesta.refreshToken() == null
                || respuesta.userId() == null || respuesta.expiresIn() == null) {
            throw new MercadoPagoOAuthException("Respuesta de token de MP incompleta");
        }
        return respuesta;
    }
}