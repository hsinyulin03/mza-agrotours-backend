package com.mza_agrotours.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * URLs que viajan en la preference de Checkout Pro.
 *
 * @param backUrl       pantalla del front a la que MP devuelve al visitante, sea cual sea el resultado del pago
 * @param notificationUrl endpoint público del webhook de pagos; vacío si no hay webhook y los pagos se consultan
 *                      por polling
 * @param webhookSecret clave secreta de la sección Webhooks de la aplicación de MP, con la que se valida la firma
 *                      de las notificaciones
 */
@ConfigurationProperties(prefix = "mercadopago.checkout")
public record MercadoPagoCheckoutProperties(String backUrl, String notificationUrl, String webhookSecret) {

    public MercadoPagoCheckoutProperties {
        if (StringUtils.hasText(notificationUrl) && !StringUtils.hasText(webhookSecret))
            throw new IllegalStateException("mercadopago.checkout.notification-url requiere mercadopago.checkout.webhook-secret");
    }

    public boolean tieneWebhook() {
        return StringUtils.hasText(notificationUrl);
    }
}
