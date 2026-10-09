package com.mza_agrotours.backend.services.pago;

import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentFeeDetail;
import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.exceptions.pago.PagoNoConciliableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Concilia un pago aprobado de Mercado Pago con la reserva que paga: valida que sea por lo esperado
 * y registra en la reserva cómo se repartió el cobro según lo que informa MP en {@code fee_details}.
 * <p></p>
 * Del {@code transaction_amount} (que no incluye el {@code financing_fee} de las cuotas, pagado por el comprador)
 * MP descuenta primero su comisión ({@code mercadopago_fee}, a cargo del vendedor) y después la de Agrotours
 * ({@code application_fee}, el {@code marketplace_fee} de la preference). El resto es del productor.
 */
@Slf4j
@Component
public class ConciliadorPagoMP {

    static final String FEE_MERCADO_PAGO = "mercadopago_fee";
    static final String FEE_MARKETPLACE = "application_fee";
    static final String FEE_PAYER_VENDEDOR = "collector";
    static final String MONEDA = "ARS";

    /**
     * @throws PagoNoConciliableException si el pago no está aprobado, no es por el total de la reserva
     *                                    en pesos o no se acreditó a la cuenta del vendedor que creó la preference
     */
    public void conciliar(Reserva reserva, Payment payment) {
        validar(reserva, payment);

        List<PaymentFeeDetail> fees = payment.getFeeDetails() == null ? List.of() : payment.getFeeDetails();

        BigDecimal comisionTransaccion = sumarFees(fees, FEE_MERCADO_PAGO, FEE_PAYER_VENDEDOR);

        // Sin cuenta del vendedor la preference es anterior al split: cobró Agrotours entero, no hay application_fee
        // y la comisión propia queda como se calculó al crear la preference
        BigDecimal comisionPropia = reserva.getSubTotalComisionPropia();
        if (reserva.getPago().getCuentaMercadoPago() != null) {
            BigDecimal comisionCobrada = sumarFees(fees, FEE_MARKETPLACE, null);
            if (comisionCobrada.compareTo(comisionPropia) != 0)
                log.warn("Pago MP {} de la reserva {}: application_fee {} distinto de la comisión esperada {}. Se registra la cobrada",
                        payment.getId(), reserva.getId(), comisionCobrada, comisionPropia);
            comisionPropia = comisionCobrada;
        }

        reserva.setSubTotalComisionTransaccion(comisionTransaccion);
        reserva.setSubTotalComisionPropia(comisionPropia);
        reserva.setSubTotalProductor(reserva.getTotalReserva().subtract(comisionTransaccion).subtract(comisionPropia));
    }

    private void validar(Reserva reserva, Payment payment) {
        if (!"approved".equals(payment.getStatus()))
            throw new PagoNoConciliableException("El pago MP %d no está aprobado (%s)"
                    .formatted(payment.getId(), payment.getStatus()));

        if (!MONEDA.equals(payment.getCurrencyId()))
            throw new PagoNoConciliableException("El pago MP %d es en %s, se esperaba %s"
                    .formatted(payment.getId(), payment.getCurrencyId(), MONEDA));

        if (payment.getTransactionAmount() == null || payment.getTransactionAmount().compareTo(reserva.getTotalReserva()) != 0)
            throw new PagoNoConciliableException("El pago MP %d es por %s y la reserva %s por %s"
                    .formatted(payment.getId(), payment.getTransactionAmount(), reserva.getId(), reserva.getTotalReserva()));

        CuentaMercadoPago cuenta = reserva.getPago().getCuentaMercadoPago();
        if (cuenta != null && !Objects.equals(cuenta.getMpUserId(), payment.getCollectorId()))
            throw new PagoNoConciliableException("El pago MP %d se acreditó al usuario %d y no al vendedor %d"
                    .formatted(payment.getId(), payment.getCollectorId(), cuenta.getMpUserId()));
    }

    /**
     * Suma los fees del tipo indicado; con {@code feePayer == null} no filtra por quién lo paga.
     */
    private BigDecimal sumarFees(List<PaymentFeeDetail> fees, String tipo, String feePayer) {
        return fees.stream()
                .filter(f -> tipo.equals(f.getType()))
                .filter(f -> feePayer == null || feePayer.equals(f.getFeePayer()))
                .map(PaymentFeeDetail::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}