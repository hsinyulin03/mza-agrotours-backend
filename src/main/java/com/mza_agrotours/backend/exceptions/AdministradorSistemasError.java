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
    LIDER_INMUTABLE("AS.lider",
            HttpStatus.FORBIDDEN,
            "No se puede mutar el estado de un administrador líder"),
    AUTO_GESTION_PROHIBIDA("AS.autoGestion",
            HttpStatus.FORBIDDEN,
            "No se puede gestionar el propio rol de administrador")
    ;
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
