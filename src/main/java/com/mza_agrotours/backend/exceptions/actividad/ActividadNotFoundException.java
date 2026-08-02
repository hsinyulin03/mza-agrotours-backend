package com.mza_agrotours.backend.exceptions.actividad;

public class ActividadNotFoundException extends RuntimeException {
    public ActividadNotFoundException(String message) {
        super(message);
    }
    public ActividadNotFoundException() {
        super("La actividad no pudo ser encontrada");
    }
}
