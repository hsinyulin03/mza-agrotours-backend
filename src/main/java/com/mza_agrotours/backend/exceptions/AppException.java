package com.mza_agrotours.backend.exceptions;

import lombok.Getter;

public class AppException extends RuntimeException {
    @Getter private final ErrorCode errorCode;
    @Getter private final Object data;
    public AppException(ErrorCode errorCode, String message, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }

    public AppException(ErrorCode errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
        this.data = null;
    }

}
