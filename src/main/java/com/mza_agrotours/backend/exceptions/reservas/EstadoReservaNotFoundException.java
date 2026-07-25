package com.mza_agrotours.backend.exceptions.reservas;

public class EstadoReservaNotFoundException extends RuntimeException {
    public EstadoReservaNotFoundException(String message) {
        super(message);
    }
    public EstadoReservaNotFoundException() {
        super("El estado de la reserva no pudo ser encontrado");
    }
}
