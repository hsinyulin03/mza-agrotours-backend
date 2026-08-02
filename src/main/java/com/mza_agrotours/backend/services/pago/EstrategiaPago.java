package com.mza_agrotours.backend.services.pago;

import com.mza_agrotours.backend.entities.pago.MetodoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.reservas.Reserva;

public interface EstrategiaPago {
    MetodoPago getMetodo();
    Pago procesarPago(Reserva reserva); // TODO cada uno coloca los subtotales
}
