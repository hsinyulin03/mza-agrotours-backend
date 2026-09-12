package com.mza_agrotours.backend.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoInit {

    @Value("${mercado.access.token}")
    private String token;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(token);
    }
}
