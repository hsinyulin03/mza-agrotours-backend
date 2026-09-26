package com.mza_agrotours.backend.exceptions.pago;

/**
 * Error al comunicarse con una pasarela de pago externa (API o red).
 */
public class PasarelaPagoException extends RuntimeException {
    public PasarelaPagoException(String message, Throwable cause) {
        super(message, cause);
    }
}