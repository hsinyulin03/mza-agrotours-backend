package com.mza_agrotours.backend.enums;

/**
 * Si un establecimiento puede recibir sus ingresos. No se persiste: se deriva de su CuentaMercadoPago vigente.
 */
public enum EstadoCobro {
    HABILITADO,
    SIN_CUENTA,
    TOKEN_VENCIDO
}
