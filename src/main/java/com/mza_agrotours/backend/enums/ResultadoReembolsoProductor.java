package com.mza_agrotours.backend.enums;

public enum ResultadoReembolsoProductor {
    REALIZADO,  // Ya estaba hecho en la pasarela y se concilió
    EN_PROCESO, // Pedido a la pasarela, se confirma con la tarea programada
    RECHAZADO   // La pasarela lo volvió a rechazar, sigue pendiente
}
