package com.mza_agrotours.backend.exceptions;

public class NotificacionNotFoundException extends RuntimeException {
    public NotificacionNotFoundException(String message) {
        super(message);
    }
    public NotificacionNotFoundException() {
        super("No se encuenetra la notificacion");}
}
