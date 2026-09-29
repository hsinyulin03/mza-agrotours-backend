package com.mza_agrotours.backend.exceptions;

public class OpenWeatherException extends RuntimeException {
    public OpenWeatherException(String message) {
        super(message);
    }

    public OpenWeatherException(String message, Throwable cause) {
        super(message, cause);
    }
}