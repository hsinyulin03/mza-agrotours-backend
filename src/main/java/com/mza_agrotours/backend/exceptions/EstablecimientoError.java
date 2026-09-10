package com.mza_agrotours.backend.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum EstablecimientoError implements ErrorCode{
    ESTABLECIMIENTO_SUSPENDIDO("E.suspendido", HttpStatus.CONFLICT, "El establecimiento está suspendido")
    ;
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
