package com.mza_agrotours.backend.exceptions;

public class UsuarioNotFound extends RuntimeException {
    public UsuarioNotFound(String message) {
        super(message);
    }
}
