package com.mza_agrotours.backend.exceptions.pago;

import com.mza_agrotours.backend.enums.EstadoReembolsoNombre;

public class ReembolsoNoImpagoException extends RuntimeException {
    public ReembolsoNoImpagoException(EstadoReembolsoNombre estado) {
        super("Solo se puede registrar el pago de un reembolso impago. Estado actual: " + estado);
    }
}
