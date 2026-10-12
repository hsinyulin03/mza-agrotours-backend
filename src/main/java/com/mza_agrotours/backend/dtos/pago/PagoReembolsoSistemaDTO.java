package com.mza_agrotours.backend.dtos.pago;

import java.time.LocalDateTime;

public record PagoReembolsoSistemaDTO(String idPago, LocalDateTime fechaHoraPago, String metodoPago) {

}
