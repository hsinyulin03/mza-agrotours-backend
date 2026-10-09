package com.mza_agrotours.backend.clients.mercadopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta de POST /oauth/token de Mercado Pago, tanto para authorization_code como para refresh_token.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoOAuthTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("public_key") String publicKey,
        @JsonProperty("user_id") Long userId,
        @JsonProperty("expires_in") Long expiresIn,     // En segundos
        @JsonProperty("live_mode") Boolean liveMode
) {
}