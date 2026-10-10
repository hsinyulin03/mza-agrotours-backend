package com.mza_agrotours.backend.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Resultados de los endpoints de cancelación de reserva, tal como los recibe el front.
 */
@Getter
@AllArgsConstructor
public enum ResultadoCancelacion {
    // Consulta previa: qué pasaría si el visitante cancela
    CANCELACION_CON_REEMBOLSO("CancelacionConReembolso"),
    CANCELACION_SIN_REEMBOLSO("CancelacionSinReembolso"),

    // Cancelación realizada
    CANCELADA_SIN_REEMBOLSO("CanceladaSinReembolso"),         // No correspondía reembolso
    REEMBOLSO_REALIZADO("ReembolsoRealizado"),                // Reembolso completado en el momento (pago manual)
    REEMBOLSO_EN_PROCESO("ReembolsoEnProceso"),               // Pedido a la pasarela, se confirma más tarde
    REEMBOLSO_MANUAL_PRODUCTOR("ReembolsoManualProductor");   // La pasarela lo rechazó, lo tiene que hacer el productor

    @JsonValue
    private final String codigo;    // Código que recibe el front
}