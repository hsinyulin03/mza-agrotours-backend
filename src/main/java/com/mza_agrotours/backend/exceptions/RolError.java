package com.mza_agrotours.backend.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum RolError implements ErrorCode{
    PERMISO_INVALIDO(
            "rol.permisoInvalido",
            HttpStatus.BAD_REQUEST,
            "No se pueden asignar los permisos solicitados");
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
