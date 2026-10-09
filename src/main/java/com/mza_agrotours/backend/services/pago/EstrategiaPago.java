package com.mza_agrotours.backend.services.pago;

import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaPagoDTO;
import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaReembolso;
import com.mza_agrotours.backend.dtos.pago.ResultadoReembolsoDTO;
import com.mza_agrotours.backend.dtos.reservas.PagoStrategyDTO;
import com.mza_agrotours.backend.enums.MetodoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.exceptions.pago.PagoNoConciliableException;
import com.mza_agrotours.backend.exceptions.pago.PasarelaPagoException;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Encapsula toda la comunicación con un medio de pago. Los servicios no deben conocer la pasarela concreta:
 * obtienen la estrategia con {@link EstrategiaPagoFactory} a partir del {@link MetodoPago} del pago.
 */
public interface EstrategiaPago {
    MetodoPago getMetodo();

    PagoStrategyDTO procesarPago(Reserva reserva); // TODO cada uno coloca los subtotales

    /**
     * Consulta a la pasarela si el pago de la reserva fue aprobado y, en tal caso, lo concilia con la reserva
     * (valida que sea por lo esperado y registra sus subtotales). No cambia el estado del pago ni de la reserva.
     *
     * @param reserva reserva cuyo pago se consulta
     * @return si fue aprobado y, en tal caso, el ID de la transacción en la pasarela
     * @throws PasarelaPagoException si falla la comunicación con la pasarela
     * @throws PagoNoConciliableException si hay un pago aprobado que no concilia con la reserva
     */
    ResultadoConsultaPagoDTO consultarPago(Reserva reserva);

    /**
     * Invalida la sesión de cobro del pago para que ya no pueda pagarse.
     *
     * @param pago pago cuya sesión de cobro se invalida
     * @param ahora fecha y hora a usar como momento de expiración
     * @throws PasarelaPagoException si falla la comunicación con la pasarela
     */
    void cancelarCheckout(Pago pago, LocalDateTime ahora);

    /**
     * Pide a la pasarela el reembolso total del pago. Si la pasarela lo rechaza, se devuelve como reembolso
     * no aceptado. Si no se puede saber si se realizó (error de comunicación), se lanza excepción.
     *
     * @param pago pago aprobado a reembolsar
     * @return si fue aceptado y, en tal caso, el ID del reembolso en la pasarela
     * @throws PasarelaPagoException si no se sabe si el reembolso se realizó
     */
    ResultadoReembolsoDTO reembolsar(Pago pago);

    /**
     * Busca en la pasarela un reembolso ya hecho del pago. Sirve para recuperar reembolsos cuyo pedido
     * quedó sin confirmar (no se guardó la respuesta de {@link #reembolsar(Pago)}).
     *
     * @param pago pago a revisar
     * @return el ID del reembolso en la pasarela, vacío si el pago no tiene reembolsos
     * @throws PasarelaPagoException si falla la comunicación con la pasarela
     */
    Optional<String> buscarReembolso(Pago pago);

    /**
     * Consulta a la pasarela el estado de un reembolso ya pedido.
     *
     * @param pago pago reembolsado
     * @param idReembolsoExterno ID del reembolso en la pasarela, devuelto por {@link #reembolsar(Pago)}
     * @return si el reembolso fue aprobado, rechazado o sigue en proceso
     * @throws PasarelaPagoException si falla la comunicación con la pasarela
     */
    ResultadoConsultaReembolso consultarReembolso(Pago pago, String idReembolsoExterno);
}