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
            "El rol de productor solicitado no se encuentra configurado"),
    NOT_FOUND("P.notFound",
            HttpStatus.NOT_FOUND,
            "No se encontró el productor"),
    YA_SUSPENDIDO("P.yaSuspendido",
            HttpStatus.CONFLICT,
            "El productor ya se encuentra suspendido"),
    NO_SUSPENDIDO("P.noSuspendido",
            HttpStatus.CONFLICT,
            "El productor no se encuentra suspendido"),
    SUSPENSION_SOBRE_BAJA("P.suspensionSobreBaja",
            HttpStatus.CONFLICT,
            "No se puede suspender a un productor dado de baja"),
    FECHA_FIN_SUSPENSION_INVALIDA("P.fechaFinSuspensionInvalida",
            HttpStatus.BAD_REQUEST,
            "La fecha de fin de la suspensión debe ser posterior al momento actual"),
    MOTIVO_REQUERIDO("P.motivoRequerido",
            HttpStatus.BAD_REQUEST,
            "Se debe indicar un motivo para el cambio de estado del productor"),
    ALREADY_EXISTS("P.alreadyExists",
            HttpStatus.CONFLICT,
            "El usuario ya es productor vigente en este establecimiento"),
    ROL_INVALIDO("P.rolInvalido",
            HttpStatus.BAD_REQUEST,
            "El rol indicado no es un rol de productor asignable en este establecimiento"),
    LIDER_INMUTABLE("P.liderInmutable",
            HttpStatus.FORBIDDEN,
            "No se puede modificar ni dar de baja al Productor Líder"),
    AUTO_GESTION_PROHIBIDA("P.autoGestionProhibida",
            HttpStatus.FORBIDDEN,
            "Un productor no puede gestionar su propia participación en el establecimiento"),
    ESTABLECIMIENTO_NOT_FOUND("P.establecimientoNotFound",
            HttpStatus.NOT_FOUND,
            "No se encontró el establecimiento")
    ;
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}