package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EstadoReservaNombre {
    PENDIENTE("Pendiente", false),
    EXPIRADA("Expirada", true),
    PAGADA("Pagada", false),
    CANCELADA_CON_REEMBOLSO("Cancelada con reembolso", true),
    CANCELADA_SIN_REEMBOLSO("Cancelada sin reembolso", true),
    CANCELADA_REEMBOLSO_PENDIENTE("Cancelada con reembolso pendiente", false),
    REEMBOLSO_NO_CONCILIACION("Reembolsada por pago no válido", true),   // El pago aprobado no coincidía con la reserva y se reembolsa
    FINALIZADA("Finalizada", true);

    private final String estado;    // Nombre lindo para mostrar en el front
    private final boolean esFinal;  // Es un estado final?
}
