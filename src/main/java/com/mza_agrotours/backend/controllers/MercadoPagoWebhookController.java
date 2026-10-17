package com.mza_agrotours.backend.controllers;

import com.mercadopago.exceptions.MPInvalidWebhookSignatureException;
import com.mercadopago.webhook.WebhookSignatureValidator;
import com.mza_agrotours.backend.config.MercadoPagoCheckoutProperties;
import com.mza_agrotours.backend.services.ReservaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Webhook de pagos de Mercado Pago, cuya URL viaja en cada preference con la reserva que paga. Es público (MP no
 * manda token de Firebase): lo protege la firma {@code x-signature}, y el payment notificado igual se vuelve a
 * consultar a MP antes de confirmar nada.
 * <p>
 * No se rechaza por antigüedad del {@code ts} de la firma: los reintentos de MP pueden traer la original, y repetir
 * una notificación no tiene efecto porque la confirmación del pago es idempotente.
 * <p>
 * Cualquier respuesta que no sea 2xx hace que MP reintente la notificación, así que solo se devuelve error cuando
 * reintentar tiene sentido.
 */
@Slf4j
@RestController
@RequestMapping("/mercadopago/webhooks")
public class MercadoPagoWebhookController {

    private final String webhookSecret;
    private final ReservaService reservaService;

    public MercadoPagoWebhookController(MercadoPagoCheckoutProperties checkoutProperties, ReservaService reservaService) {
        this.webhookSecret = checkoutProperties.webhookSecret();
        this.reservaService = reservaService;
    }

    @PostMapping("/pagos")
    public ResponseEntity<Void> notificarPago(@RequestHeader(value = "x-signature", required = false) String xSignature,
                                              @RequestHeader(value = "x-request-id", required = false) String xRequestId,
                                              @RequestParam(value = "data.id", required = false) String dataId,
                                              @RequestParam(required = false) String type,
                                              @RequestParam(required = false) String reservaId) {
        if (!StringUtils.hasText(webhookSecret)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            // MP firma el data.id en minúsculas y el validador del SDK (3.7.0) no lo normaliza
            WebhookSignatureValidator.validate(xSignature, xRequestId, dataId == null ? null : dataId.toLowerCase(), webhookSecret);
        } catch (MPInvalidWebhookSignatureException e) {
            log.warn("Notificación de MP con firma inválida (data.id={}, reservaId={}): {}", dataId, reservaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!"payment".equals(type) || dataId == null) return ResponseEntity.ok().build();

        UUID idReserva;
        try {
            idReserva = UUID.fromString(reservaId);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Notificación del payment MP {} sin una reserva válida: '{}'", dataId, reservaId);
            return ResponseEntity.ok().build();
        }

        try {
            reservaService.confirmarPagoNotificado(idReserva, dataId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error procesando la notificación del payment MP {} de la reserva {}, MP la reintentará", dataId, idReserva, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
