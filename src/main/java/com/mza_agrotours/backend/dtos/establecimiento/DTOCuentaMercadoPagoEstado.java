package com.mza_agrotours.backend.dtos.establecimiento;

import java.time.LocalDateTime;

/**
 * Estado de la vinculación de la cuenta de Mercado Pago de un establecimiento.
 * Nunca expone tokens: solo lo necesario para que el front sepa si mostrar "Vincular".
 */
public record DTOCuentaMercadoPagoEstado(
        boolean vinculada,
        LocalDateTime fechaHoraVinculacion,
        LocalDateTime fechaHoraExpiracionToken
) {
    public static DTOCuentaMercadoPagoEstado sinVincular() {
        return new DTOCuentaMercadoPagoEstado(false, null, null);
    }
}