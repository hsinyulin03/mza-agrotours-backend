package com.mza_agrotours.backend.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum CalificacionError implements ErrorCode{
    RESERVA_NO_FINALIZADA("CAL.reservaNoFinalizada",
            HttpStatus.CONFLICT,
            "Solo podés valorar experiencias de reservas finalizadas"),
    YA_CALIFICADA("CAL.yaCalificada",
            HttpStatus.CONFLICT,
            "Ya valoraste esta experiencia");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
