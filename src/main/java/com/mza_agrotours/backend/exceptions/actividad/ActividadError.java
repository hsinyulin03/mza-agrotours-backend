package com.mza_agrotours.backend.exceptions.actividad;

import com.mza_agrotours.backend.exceptions.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum ActividadError implements ErrorCode {
    ACTIVIDAD_CON_RESERVAS_PAGADAS("A.reservasPagadas", HttpStatus.CONFLICT,
            "Existen reservas asociadas en estado «Pagado». " +
            "Debe dirigirse al detalle de esta actividad y pasar todas las reservas a estado «Cancelado con reembolso» " +
            "y gestionar los reembolsos correspondientes antes de proceder."),
    ACTIVIDAD_CON_RESERVAS_ACTIVAS("A.conReservasActivas", HttpStatus.CONFLICT,
            "La actividad posee reservas en estado pendiente o pagada");
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
