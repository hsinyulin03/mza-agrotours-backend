package com.mza_agrotours.backend.exceptions;

public class EstablecimientoNotFoundException extends RuntimeException {
    public EstablecimientoNotFoundException(String message) {
        super(message);
    }
    public EstablecimientoNotFoundException() {
        super("El establecimiento no fue encontrado");
    }
}
