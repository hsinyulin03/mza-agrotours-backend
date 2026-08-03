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
            "El administrador de sistemas no fue encontrado"),
    ALREADY_EXISTS("AS.alreadyExists",
            HttpStatus.CONFLICT,
            "El administrador de sistemas ya existe"),
    ROL_INVALIDO("AS.rolInvalido",
            HttpStatus.BAD_REQUEST,
            "El rol no es un rol de administrador asignable"),
    ;
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
