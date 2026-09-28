package com.mza_agrotours.backend.exceptions;

public class FailedFirebaseChatOperationException extends Exception {
    public FailedFirebaseChatOperationException(String message) {
        super(message);
    }

    public FailedFirebaseChatOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
