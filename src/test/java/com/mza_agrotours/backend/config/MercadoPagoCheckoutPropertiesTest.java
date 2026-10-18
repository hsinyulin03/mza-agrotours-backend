package com.mza_agrotours.backend.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MercadoPagoCheckoutPropertiesTest {

    @Test
    void unWebhookSinClaveNoArranca() {
        assertThrows(IllegalStateException.class, () -> new MercadoPagoCheckoutProperties("http://front", "https://api/webhook", ""));
    }

    @Test
    void sinNotificationUrlNoHayWebhookNiHaceFaltaClave() {
        assertFalse(new MercadoPagoCheckoutProperties("http://front", "", null).tieneWebhook());
    }
}
