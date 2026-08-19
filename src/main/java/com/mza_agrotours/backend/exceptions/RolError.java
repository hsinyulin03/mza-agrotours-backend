package com.mza_agrotours.backend.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum RolError implements ErrorCode{
    NOT_FOUND("rol.notFound",
            HttpStatus.NOT_FOUND,
            "El rol no fue encontrado"),
    ROL_ALREADY_EXISTS("rol.rolAlreadyExists",
            HttpStatus.FORBIDDEN,
            "Ya existe un rol con ese nombre"),
    BAJA_ROL_CON_USUARIOS("rol.bajaTieneUsuarios",
            HttpStatus.FORBIDDEN,
            "No se puede dar de baja un rol con usuarios asignados"),
    PERMISO_INVALIDO(
            "rol.permisoInvalido",
            HttpStatus.BAD_REQUEST,
            "No se pueden asignar los permisos solicitados");
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
