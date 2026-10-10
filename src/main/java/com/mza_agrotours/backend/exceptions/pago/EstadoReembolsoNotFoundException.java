package com.mza_agrotours.backend.exceptions.pago;


import com.mza_agrotours.backend.enums.EstadoReembolsoNombre;

public class EstadoReembolsoNotFoundException extends RuntimeException {
    public EstadoReembolsoNotFoundException(String message) {
        super(message);
    }

    public EstadoReembolsoNotFoundException() {
        super("El estado del reembolso no pudo ser encontrado");
    }

    public EstadoReembolsoNotFoundException(EstadoReembolsoNombre estadoNombre) {
        super("El estado del reembolso no pudo ser encontrado: " + estadoNombre);
    }
}