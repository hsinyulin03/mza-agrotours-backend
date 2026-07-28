package com.mza_agrotours.backend.exceptions.reservas;

public class FechaNacimientoInvalidaException extends RuntimeException {
    public FechaNacimientoInvalidaException(String message) {
        super(message);
    }
    public FechaNacimientoInvalidaException() {
        super("La fecha de nacimiento ingresada no es válida");
    }
}
