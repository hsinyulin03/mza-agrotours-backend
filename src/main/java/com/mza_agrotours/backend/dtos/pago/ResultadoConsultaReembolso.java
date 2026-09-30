package com.mza_agrotours.backend.dtos.pago;

/**
 * Resultado de consultar a la pasarela el estado de un reembolso ya pedido.
 */
public enum ResultadoConsultaReembolso {
    APROBADO,   // El dinero fue devuelto al visitante
    RECHAZADO,  // La pasarela no pudo hacer el reembolso, queda en manos del productor
    EN_PROCESO  // Todavía no hay respuesta final, se vuelve a consultar más tarde
}