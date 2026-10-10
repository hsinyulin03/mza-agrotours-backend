package com.mza_agrotours.backend.exceptions.pago;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Un pago aprobado en Mercado Pago no coincide con la reserva que paga (monto, moneda o vendedor).
 * La reserva no se confirma y el pago se reembolsa, por eso la excepción lleva su ID y el monto pagado.
 */
@Getter
public class PagoNoConciliableException extends RuntimeException {
    private final String idTransaccionExterna;  // ID del payment de MP que no concilia
    private final BigDecimal montoPagado;       // Monto realmente cobrado, null si MP no lo informó

    public PagoNoConciliableException(String message, String idTransaccionExterna, BigDecimal montoPagado) {
        super(message);
        this.idTransaccionExterna = idTransaccionExterna;
        this.montoPagado = montoPagado;
    }
}