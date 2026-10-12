package com.mza_agrotours.backend.exceptions.pago;

public class ReembolsoNotFoundException extends RuntimeException {
    public ReembolsoNotFoundException() {
        super("El reembolso no pudo ser encontrado");
    }
}
