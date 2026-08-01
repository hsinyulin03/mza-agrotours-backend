package com.mza_agrotours.backend.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum AdministradorSistemasError implements ErrorCode {
    NOT_FOUND("AS.notFound",
            HttpStatus.NOT_FOUND,
            "El administrador de sistemas no fue encontrado");
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
