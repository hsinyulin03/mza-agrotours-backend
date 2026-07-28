package com.mza_agrotours.backend.exceptions;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String code();
    HttpStatus httpStatus();
    String defaultMessage();
}
