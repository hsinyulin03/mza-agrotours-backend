package com.mza_agrotours.backend.exceptions;

public class FailedToGenerateResourceSignedUrlException extends RuntimeException {
    public FailedToGenerateResourceSignedUrlException(String message) {
        super(message);
    }
}
