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
            "No se puede gestionar el propio rol de administrador"),
    ESTABLECIMIENTO_NOT_FOUND("AS.establecimientoNotFound",
            HttpStatus.NOT_FOUND,
            "No se encontró el establecimiento"),
    ESTABLECIMIENTO_NO_ACTIVO("AS.establecimientoNoActivo",
            HttpStatus.CONFLICT,
            "El establecimiento no se encuentra activo"),
    ESTABLECIMIENTO_NO_SUSPENDIDO("AS.establecimientoNoSuspendido",
            HttpStatus.CONFLICT,
            "El establecimiento no se encuentra suspendido"),
    ESTADO_ESTABLECIMIENTO_NO_CONFIGURADO("AS.estadoEstablecimientoNoConfigurado",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "El estado de establecimiento solicitado no se encuentra configurado")
    ;
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
