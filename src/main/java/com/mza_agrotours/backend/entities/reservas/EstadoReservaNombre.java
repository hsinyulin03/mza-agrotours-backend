package com.mza_agrotours.backend.entities.reservas;

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
    FINALIZADA("Finalizada", true);

    private final String estado;    // Nombre lindo para mostrar en el front
    private final boolean esFinal;  // Es un estado final?
}
