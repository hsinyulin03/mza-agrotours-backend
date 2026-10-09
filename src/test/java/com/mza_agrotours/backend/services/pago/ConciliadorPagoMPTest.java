package com.mza_agrotours.backend.services.pago;

import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentFeeDetail;
import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.exceptions.pago.PagoNoConciliableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConciliadorPagoMPTest {

    private static final long VENDEDOR = 123456L;

    private final ConciliadorPagoMP conciliador = new ConciliadorPagoMP();

    private Reserva reserva;
    private Pago pago;

    @BeforeEach
    void setUp() {
        CuentaMercadoPago cuenta = new CuentaMercadoPago();
        cuenta.setMpUserId(VENDEDOR);

        pago = new Pago();
        pago.setCuentaMercadoPago(cuenta);

        // Total 10000 con 10% de comisión calculada al crear la preference
        reserva = new Reserva();
        reserva.setTotalReserva(new BigDecimal("10000.00"));
        reserva.setSubTotalComisionPropia(new BigDecimal("1000.00"));
        reserva.setSubTotalProductor(new BigDecimal("9000.00"));
        reserva.setPago(pago);
    }

    private static PaymentFeeDetail fee(String tipo, String feePayer, String monto) {
        PaymentFeeDetail fee = mock(PaymentFeeDetail.class);
        when(fee.getType()).thenReturn(tipo);
        when(fee.getFeePayer()).thenReturn(feePayer);
        when(fee.getAmount()).thenReturn(new BigDecimal(monto));
        return fee;
    }

    private static Payment payment(String status, String moneda, String monto, Long collector, List<PaymentFeeDetail> fees) {
        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(987L);
        when(payment.getStatus()).thenReturn(status);
        when(payment.getCurrencyId()).thenReturn(moneda);
        when(payment.getTransactionAmount()).thenReturn(monto == null ? null : new BigDecimal(monto));
        when(payment.getCollectorId()).thenReturn(collector);
        when(payment.getFeeDetails()).thenReturn(fees);
        return payment;
    }

    private static Payment aprobado(List<PaymentFeeDetail> fees) {
        return payment("approved", "ARS", "10000", VENDEDOR, fees);
    }

    @Test
    void registraLasComisionesInformadasPorMP() {
        conciliador.conciliar(reserva, aprobado(List.of(
                fee("mercadopago_fee", "collector", "629.00"),
                fee("application_fee", "collector", "1000.00"))));

        assertEquals(0, new BigDecimal("629.00").compareTo(reserva.getSubTotalComisionTransaccion()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(reserva.getSubTotalComisionPropia()));
        assertEquals(0, new BigDecimal("8371.00").compareTo(reserva.getSubTotalProductor()));
    }

    @Test
    void ignoraElFinancingFeeQuePagaElComprador() {
        conciliador.conciliar(reserva, aprobado(List.of(
                fee("financing_fee", "payer", "2130.00"),
                fee("mercadopago_fee", "collector", "629.00"),
                fee("application_fee", "collector", "1000.00"))));

        assertEquals(0, new BigDecimal("629.00").compareTo(reserva.getSubTotalComisionTransaccion()));
        assertEquals(0, new BigDecimal("8371.00").compareTo(reserva.getSubTotalProductor()));
    }

    @Test
    void registraElApplicationFeeCobradoAunqueDifieraDelEsperado() {
        conciliador.conciliar(reserva, aprobado(List.of(
                fee("mercadopago_fee", "collector", "629.00"),
                fee("application_fee", "collector", "950.00"))));

        assertEquals(0, new BigDecimal("950.00").compareTo(reserva.getSubTotalComisionPropia()));
        assertEquals(0, new BigDecimal("8421.00").compareTo(reserva.getSubTotalProductor()));
    }

    @Test
    void sinCuentaDelVendedorMantieneLaComisionCalculada() {
        // Preference anterior al split: cobra Agrotours, no hay application_fee ni vendedor que validar
        pago.setCuentaMercadoPago(null);

        conciliador.conciliar(reserva, payment("approved", "ARS", "10000", 999L,
                List.of(fee("mercadopago_fee", "collector", "629.00"))));

        assertEquals(0, new BigDecimal("1000.00").compareTo(reserva.getSubTotalComisionPropia()));
        assertEquals(0, new BigDecimal("8371.00").compareTo(reserva.getSubTotalProductor()));
    }

    @Test
    void sinFeeDetailsNoHayComisionDeTransaccion() {
        conciliador.conciliar(reserva, aprobado(null));

        assertEquals(0, BigDecimal.ZERO.compareTo(reserva.getSubTotalComisionTransaccion()));
        assertEquals(0, BigDecimal.ZERO.compareTo(reserva.getSubTotalComisionPropia()));
        assertEquals(0, new BigDecimal("10000.00").compareTo(reserva.getSubTotalProductor()));
    }

    @Test
    void rechazaUnPagoNoAprobado() {
        assertThrows(PagoNoConciliableException.class, () ->
                conciliador.conciliar(reserva, payment("pending", "ARS", "10000", VENDEDOR, List.of())));
    }

    @Test
    void rechazaUnPagoPorOtroMonto() {
        assertThrows(PagoNoConciliableException.class, () ->
                conciliador.conciliar(reserva, payment("approved", "ARS", "9999.99", VENDEDOR, List.of())));
    }

    @Test
    void rechazaUnPagoSinMonto() {
        assertThrows(PagoNoConciliableException.class, () ->
                conciliador.conciliar(reserva, payment("approved", "ARS", null, VENDEDOR, List.of())));
    }

    @Test
    void rechazaUnPagoEnOtraMoneda() {
        assertThrows(PagoNoConciliableException.class, () ->
                conciliador.conciliar(reserva, payment("approved", "USD", "10000", VENDEDOR, List.of())));
    }

    @Test
    void rechazaUnPagoAcreditadoAOtroVendedor() {
        assertThrows(PagoNoConciliableException.class, () ->
                conciliador.conciliar(reserva, payment("approved", "ARS", "10000", 999L, List.of())));
    }

    @Test
    void noModificaLaReservaSiRechazaElPago() {
        assertThrows(PagoNoConciliableException.class, () ->
                conciliador.conciliar(reserva, payment("approved", "ARS", "1", VENDEDOR,
                        List.of(fee("mercadopago_fee", "collector", "629.00")))));

        assertNull(reserva.getSubTotalComisionTransaccion());
        assertEquals(0, new BigDecimal("9000.00").compareTo(reserva.getSubTotalProductor()));
    }
}