package com.mza_agrotours.backend.entities.pago;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EstadoPagoNombre{
    PENDIENTE("Pendiente"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado");

    private final String estado;    // Nombre lindo para mostrar en el front
}
