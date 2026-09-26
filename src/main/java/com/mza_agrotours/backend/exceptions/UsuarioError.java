package com.mza_agrotours.backend.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum UsuarioError implements ErrorCode {
    USUARIO_ALREADY_EXISTS("USR.alreadyExists",
            HttpStatus.CONFLICT,
            "Ya existe un usuario con ese email"),
    USUARIO_NOT_FOUND("USR.notFound",
            HttpStatus.NOT_FOUND,
            "No se ha encontrado el usuario");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

}
