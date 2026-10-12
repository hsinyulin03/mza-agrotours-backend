package com.mza_agrotours.backend.exceptions.pago;

import com.mza_agrotours.backend.enums.EstadoReembolsoNombre;

public class ReembolsoNoPendienteException extends RuntimeException {
    public ReembolsoNoPendienteException(EstadoReembolsoNombre estado) {
        super("El reembolso no está pendiente de realizarse por el productor. Estado del reembolso: " + estado);
    }
}
