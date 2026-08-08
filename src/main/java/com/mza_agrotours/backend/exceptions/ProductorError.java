package com.mza_agrotours.backend.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum ProductorError implements ErrorCode {
    ESTADO_NO_CONFIGURADO("P.estadoNoConfigurado",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "El estado de productor solicitado no se encuentra configurado"),
    ROL_NO_CONFIGURADO("P.rolNoConfigurado",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "El rol de productor solicitado no se encuentra configurado")
    ;
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}