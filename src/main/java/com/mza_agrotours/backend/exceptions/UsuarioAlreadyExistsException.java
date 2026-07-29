package com.mza_agrotours.backend.exceptions;

public class UsuarioAlreadyExistsException  extends Exception {
    public UsuarioAlreadyExistsException(String message) {
        super(message);
    }
}
