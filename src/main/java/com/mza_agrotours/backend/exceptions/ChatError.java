package com.mza_agrotours.backend.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum ChatError implements ErrorCode {
    CHAT_YA_EXISTE("CHAT.existente",
            HttpStatus.CONFLICT,
            "El chat ya existe");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
