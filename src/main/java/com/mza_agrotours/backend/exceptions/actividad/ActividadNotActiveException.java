package com.mza_agrotours.backend.exceptions.actividad;

public class ActividadNotActiveException extends RuntimeException {
    public ActividadNotActiveException(String message) {
        super(message);
    }
    public ActividadNotActiveException() {
        super("La actividad no se encuentra disponible");
    }
}
