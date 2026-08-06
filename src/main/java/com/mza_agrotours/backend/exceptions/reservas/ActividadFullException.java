package com.mza_agrotours.backend.exceptions.reservas;

public class ActividadFullException extends RuntimeException {
    public ActividadFullException(String message) {
        super(message);
    }
    public ActividadFullException() {
        super("No se puede reservar, la actividad no tiene cupos suficientes para este día");
    }
}
