package com.mza_agrotours.backend.dtos.pago;

/**
 * Resultado de pedir un reembolso a la pasarela.
 *
 * @param aceptado si la pasarela aceptó el reembolso
 * @param idReembolsoExterno ID del reembolso en la pasarela, null si no fue aceptado o el método no usa pasarela
 */
public record ResultadoReembolsoDTO(boolean aceptado, String idReembolsoExterno) {
    public static ResultadoReembolsoDTO rechazado() {
        return new ResultadoReembolsoDTO(false, null);
    }
}