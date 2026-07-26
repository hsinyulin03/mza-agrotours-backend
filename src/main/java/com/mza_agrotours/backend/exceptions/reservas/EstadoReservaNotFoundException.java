package com.mza_agrotours.backend.exceptions.reservas;

import com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre;

public class EstadoReservaNotFoundException extends RuntimeException {
    public EstadoReservaNotFoundException(String message) {
        super(message);
    }

    public EstadoReservaNotFoundException() {
        super("El estado de la reserva no pudo ser encontrado");
    }

    public EstadoReservaNotFoundException(EstadoReservaNombre estadoNombre) {
        super("El estado de la reserva no pudo ser encontrado: " + estadoNombre.getEstado());
    }
}
