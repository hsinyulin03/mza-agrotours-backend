package com.mza_agrotours.backend.exceptions.tipoCultivo;

import com.mza_agrotours.backend.exceptions.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TipoCultivoError implements ErrorCode {
    VALIDACION_MULTIPLE(
            "tipoCultivoValidacionMultiple",
            HttpStatus.BAD_REQUEST,
            "Por favor, revisá los datos ingresados."
    ),
    NOT_FOUND(
            "tipoCultivoNotFound",
            HttpStatus.NOT_FOUND,
            "No se encuentra el tipo de cultivo indicado."
    );

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    @Override
    public String code() {
        return code;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

}
