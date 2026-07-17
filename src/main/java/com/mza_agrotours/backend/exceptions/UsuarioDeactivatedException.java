package com.mza_agrotours.backend.exceptions;

public class UsuarioDeactivatedException extends RuntimeException {
    public UsuarioDeactivatedException(String message) {
        super(message);
    }
    public UsuarioDeactivatedException() {
        super("El usuario está dado de baja");
    }
}
