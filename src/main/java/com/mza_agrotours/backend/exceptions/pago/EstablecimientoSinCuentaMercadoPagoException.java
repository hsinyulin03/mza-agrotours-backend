package com.mza_agrotours.backend.exceptions.pago;

public class EstablecimientoSinCuentaMercadoPagoException extends RuntimeException {
    public EstablecimientoSinCuentaMercadoPagoException() {
        super("El establecimiento no tiene una cuenta de Mercado Pago vinculada para cobrar");
    }
}