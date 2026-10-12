package com.mza_agrotours.backend.dtos.pago;

import java.time.LocalDateTime;

public record PagoReembolsoSistemaDTO(
        // Pago - id, fecha y hora de la transacción, id externo
        String idPago, LocalDateTime fechaHoraPago, String idPagoExterno,

        // ReembolsoSistemaPago - medio con el que se pagó realmente y cuándo se cargó
        String metodoPago, LocalDateTime fechaHoraCarga,

        // Nombre del administrador que lo cargó
        String administrador
) {

}
