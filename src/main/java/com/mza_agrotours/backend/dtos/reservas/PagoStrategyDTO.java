package com.mza_agrotours.backend.dtos.reservas;

import com.mza_agrotours.backend.entities.pago.Pago;

public record PagoStrategyDTO(Pago pago, String preferenceID) {
}
