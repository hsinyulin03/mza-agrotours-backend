package com.mza_agrotours.backend.exceptions.pago;

import com.mza_agrotours.backend.enums.EstadoPagoNombre;

public class EstadoPagoNotFoundException extends RuntimeException {
    public EstadoPagoNotFoundException(String message) {
        super(message);
    }

    public EstadoPagoNotFoundException() {
        super("El estado del pago no pudo ser encontrado");
    }

    public EstadoPagoNotFoundException(EstadoPagoNombre estadoNombre) {
        super("El estado del pago no pudo ser encontrado: " + estadoNombre.getEstado());
    }
}