package com.mza_agrotours.backend.exceptions.actividad;

public class ActividadDiaNotFound extends RuntimeException {
    public ActividadDiaNotFound(String message) {
        super(message);
    }
    public ActividadDiaNotFound() {
        super("El día seleccionado no pudo ser hallado para esta actividad");
    }
}
