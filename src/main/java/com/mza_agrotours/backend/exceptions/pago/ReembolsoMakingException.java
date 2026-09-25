package com.mza_agrotours.backend.exceptions.pago;


public class ReembolsoMakingException extends RuntimeException {
    public ReembolsoMakingException(String message) {
        super(message);
    }

    public ReembolsoMakingException() {
        super("El reembolso no pudo ser realizado debido a un error");
    }
}