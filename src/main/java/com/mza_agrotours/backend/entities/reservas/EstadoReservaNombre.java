package com.mza_agrotours.backend.entities.reservas;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EstadoReservaNombre {
    PENDIENTE("Pendiente"),
    EXPIRADA("Expirada"),
    PAGADA("Pagada"),
    CANCELADA_CON_REEMBOLSO("Cancelada con reembolso"),
    CANCELADA_SIN_REEMBOLSO("Cancelada sin reembolso"),
    FINALIZADA("Finalizada");

    private final String estado;
}
