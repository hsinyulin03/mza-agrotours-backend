package com.mza_agrotours.backend.exceptions;

public class FailedToDeleteMiembroEstablecimientoException extends Exception {
    public FailedToDeleteMiembroEstablecimientoException(String message) {
        super(message);
    }

    public FailedToDeleteMiembroEstablecimientoException(String message, Throwable cause) {
        super(message, cause);
    }
}
