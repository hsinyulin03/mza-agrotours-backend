package com.mza_agrotours.backend.exceptions.pago;

public class MercadoPagoOAuthException extends RuntimeException {
    public MercadoPagoOAuthException(String message) {
        super(message);
    }

    public MercadoPagoOAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}