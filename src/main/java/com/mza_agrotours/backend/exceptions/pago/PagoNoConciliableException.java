package com.mza_agrotours.backend.exceptions.pago;

/**
 * Un pago aprobado en Mercado Pago no coincide con la reserva que paga (monto, moneda o vendedor).
 * La reserva no se confirma y el pago queda para revisión manual.
 */
public class PagoNoConciliableException extends RuntimeException {
    public PagoNoConciliableException(String message) {
        super(message);
    }
}