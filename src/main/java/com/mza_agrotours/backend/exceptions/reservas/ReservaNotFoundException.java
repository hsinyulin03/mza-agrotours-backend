package com.mza_agrotours.backend.exceptions.reservas;

public class ReservaNotFoundException extends RuntimeException {
    public ReservaNotFoundException(String message) {
        super(message);
    }
    public ReservaNotFoundException() {
        super("La reserva no pudo ser encontrada");
    }
}
