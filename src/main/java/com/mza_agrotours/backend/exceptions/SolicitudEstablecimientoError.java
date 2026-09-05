package com.mza_agrotours.backend.exceptions;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum SolicitudEstablecimientoError implements ErrorCode{
    NOT_FOUND("SE.notFound",
            HttpStatus.NOT_FOUND,
            "La solicitud del establecimiento no fue encontrada"),
    ESTABLECIMIENTO_ALREADY_EXISTS("SE.establecimientoAlreadyExists",
            HttpStatus.FORBIDDEN,
            "Ya existe un establecimiento vigente con ese CUIT"),
    SOLICITUD_ESTABLECIMIENTO_ALREADY_EXISTS("SE.SEAlreadyExists",
            HttpStatus.FORBIDDEN,
    "Ya existe una solicitud de este establecimiento"),
    ESTADO_INVALIDO("SE.InvalidState", HttpStatus.BAD_REQUEST, "El estado enviado es inválido");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
