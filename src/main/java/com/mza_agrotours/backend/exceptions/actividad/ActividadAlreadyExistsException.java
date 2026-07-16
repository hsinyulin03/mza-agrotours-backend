package com.mza_agrotours.backend.exceptions.actividad;

public class ActividadAlreadyExistsException extends RuntimeException {
    public ActividadAlreadyExistsException(String message) {
        super(message);
    }
}
