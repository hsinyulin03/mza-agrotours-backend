package com.mza_agrotours.backend.exceptions;

import lombok.Getter;

@Getter
public class ObjectStorageProviderException extends Exception {
    private final String code;

    public ObjectStorageProviderException(String message, String code) {
        super(message);
        this.code = code;
    }
}
