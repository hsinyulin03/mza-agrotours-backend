package com.mza_agrotours.backend.exceptions;

public class FailedToAddMiembroEstablecimientoException extends Exception {
    public FailedToAddMiembroEstablecimientoException(String message) {
        super(message);
    }

    public FailedToAddMiembroEstablecimientoException(String message, Throwable cause) {
        super(message, cause);
    }
}
