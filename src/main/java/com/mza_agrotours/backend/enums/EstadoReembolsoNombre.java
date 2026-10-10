package com.mza_agrotours.backend.enums;

public enum EstadoReembolsoNombre {
    EN_PROCESO, // Esperando confirmación de la pasarela de pago
    PEDIDO, // El productor tiene que hacer click manual en reembolsar
    IMPAGO,
    REEMBOLSADO_PRODUCTOR,
    REEMBOLSADO_SISTEMA
}
