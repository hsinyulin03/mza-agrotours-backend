package com.mza_agrotours.backend.enums;

/**
 * Cambio de estado de una deuda que pide un administrador. DEVOLUCION lleva pago obligatorio y la deuda pasa a
 * DEVOLUCION_PARCIAL o DEVOLUCION_TOTAL según lo devuelto; ACUERDO_MEDIO la cierra, con pago opcional.
 */
public enum TipoCambioEstadoDeuda {
    DEVOLUCION,
    ACUERDO_MEDIO
}
