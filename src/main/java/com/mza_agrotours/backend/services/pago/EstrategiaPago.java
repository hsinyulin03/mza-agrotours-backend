package com.mza_agrotours.backend.services.pago;

import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaPagoDTO;
import com.mza_agrotours.backend.dtos.pago.ResultadoReembolsoDTO;
import com.mza_agrotours.backend.dtos.reservas.PagoStrategyDTO;
import com.mza_agrotours.backend.enums.MetodoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.exceptions.pago.PasarelaPagoException;

import java.time.LocalDateTime;

/**
 * Encapsula toda la comunicación con un medio de pago. Los servicios no deben conocer la pasarela concreta:
 * obtienen la estrategia con {@link EstrategiaPagoFactory} a partir del {@link MetodoPago} del pago.
 */
public interface EstrategiaPago {
    MetodoPago getMetodo();

    PagoStrategyDTO procesarPago(Reserva reserva); // TODO cada uno coloca los subtotales

    /**
     * Consulta a la pasarela si el pago fue aprobado.
     *
     * @param pago pago a consultar
     * @return si fue aprobado y, en tal caso, el ID de la transacción en la pasarela
     * @throws PasarelaPagoException si falla la comunicación con la pasarela
     */
    ResultadoConsultaPagoDTO consultarPago(Pago pago);

    /**
     * Invalida la sesión de cobro del pago para que ya no pueda pagarse.
     *
     * @param pago pago cuya sesión de cobro se invalida
     * @param ahora fecha y hora a usar como momento de expiración
     * @throws PasarelaPagoException si falla la comunicación con la pasarela
     */
    void cancelarCheckout(Pago pago, LocalDateTime ahora);

    /**
     * Pide a la pasarela el reembolso total del pago. Los errores de la pasarela no se lanzan:
     * se devuelven como reembolso no aceptado.
     *
     * @param pago pago aprobado a reembolsar
     * @return si fue aceptado y, en tal caso, el ID del reembolso en la pasarela
     */
    ResultadoReembolsoDTO reembolsar(Pago pago);
}