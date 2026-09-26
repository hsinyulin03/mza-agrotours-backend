package com.mza_agrotours.backend.dtos.pago;

/**
 * Resultado de consultar a la pasarela el estado de un pago.
 *
 * @param aprobado si el pago fue aprobado
 * @param idTransaccionExterna ID de la transacción aprobada en la pasarela, null si no fue aprobado o el método no usa pasarela
 */
public record ResultadoConsultaPagoDTO(boolean aprobado, String idTransaccionExterna) {
    public static ResultadoConsultaPagoDTO noAprobado() {
        return new ResultadoConsultaPagoDTO(false, null);
    }
}