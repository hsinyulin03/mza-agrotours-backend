package com.mza_agrotours.backend.exceptions;

public class PaisNotFoundException extends RuntimeException {
    public PaisNotFoundException(String message) {
        super(message);
    }
    public PaisNotFoundException() {
        super("No se pudo encontrar el pais");
    }
}
