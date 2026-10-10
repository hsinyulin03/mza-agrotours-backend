package com.mza_agrotours.backend.exceptions.pago;


public class ReembolsoDateException extends RuntimeException {
    public ReembolsoDateException(String fhFinActividad, String ahora) {
        super("El reembolso no pudo ser realizado debido a ser una actividad cuya fecha de fin ya trasncurrió. Fecha hora fin de la actividad: " + fhFinActividad + " - Fecha hora actual: " + ahora);
    }

    public ReembolsoDateException() {
        super("El reembolso no pudo ser realizado debido a ser una actividad cuya fecha de fin ya trasncurrió.");
    }
}