package com.mza_agrotours.backend.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum ReservaError implements ErrorCode {
    ACTIVIDAD_FULL("Actividad.FULL",
            HttpStatus.CONFLICT,
            "No se puede reservar, la actividad no tiene cupos suficientes para este día"),
    ;
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}