package com.mza_agrotours.backend.exceptions.pago;


public class ReembolsoStateException extends RuntimeException {
    public ReembolsoStateException(String estado) {
        super("El reembolso no pudo ser realizado por no estar la reserva en estado Pagada. Estado de la reserva: " + estado);
    }

    public ReembolsoStateException() {
        super("El reembolso no pudo ser realizado por no estar la reserva en estado Pagada.");
    }
}