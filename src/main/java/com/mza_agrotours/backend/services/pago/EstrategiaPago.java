package com.mza_agrotours.backend.services.pago;

import com.mza_agrotours.backend.dtos.reservas.PagoStrategyDTO;
import com.mza_agrotours.backend.enums.MetodoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.reservas.Reserva;

public interface EstrategiaPago {
    MetodoPago getMetodo();
    PagoStrategyDTO procesarPago(Reserva reserva); // TODO cada uno coloca los subtotales
}
