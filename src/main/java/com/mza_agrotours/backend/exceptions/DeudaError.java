package com.mza_agrotours.backend.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum DeudaError implements ErrorCode {
    DEUDA_FINALIZADA("Deuda.finalizada",
            HttpStatus.CONFLICT,
            "La deuda ya fue devuelta o cerrada con un acuerdo"),
    PAGO_OBLIGATORIO("Deuda.pagoObligatorio",
            HttpStatus.BAD_REQUEST,
            "Una devolución tiene que tener un pago"),
    MONTO_INVALIDO("Deuda.montoInvalido",
            HttpStatus.BAD_REQUEST,
            "El monto del pago no puede superar lo que resta devolver de la deuda"),
    PAGO_DUPLICADO("Deuda.pagoDuplicado",
            HttpStatus.CONFLICT,
            "Ya se registró un pago con ese ID externo para esta deuda"),
    ;
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
