package com.mza_agrotours.backend.services;

import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPElementsResourcesPage;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.resources.merchantorder.MerchantOrder;
import com.mercadopago.resources.merchantorder.MerchantOrderPayment;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentFeeDetail;
import com.mza_agrotours.backend.clients.mercadopago.MercadoPagoOAuthClient;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.EstadoPagoNombre;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.enums.MetodoPago;
import com.mza_agrotours.backend.mappers.reserva.ReservaMapper;
import com.mza_agrotours.backend.repositories.*;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoPagoRepository;
import com.mza_agrotours.backend.security.cifrado.Cifrador;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import com.mza_agrotours.backend.services.pago.ConciliadorPagoMP;
import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService;
import com.mza_agrotours.backend.services.pago.EstrategiaPagoFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Llamadas a Mercado Pago de ReservaService con el split de marketplace: confirmación de pagos por el scheduler
 * (conciliación) y expiración de preferences, siempre con el token de la cuenta del vendedor que creó la preference.
 * Los clientes del SDK se instancian dentro de los métodos, así que se interceptan con {@code mockConstruction}.
 */
class ReservaServicePagoMPTest {

    private static final String TOKEN_VENDEDOR = "APP_USR-token-del-vendedor";
    private static final long VENDEDOR = 123456L;

    private ReservaRepository reservaRepository;
    private UsuarioRepository usuarioRepository;
    private VisitanteRepository visitanteRepository;
    private ReservaService self;
    private ReservaService service;

    private final EstadoReserva estadoPagada = new EstadoReserva(EstadoReservaNombre.PAGADA);
    private final EstadoReserva estadoExpirada = new EstadoReserva(EstadoReservaNombre.EXPIRADA);

    // Respuestas de MP por preference: merchant orders con sus pagos, y el detalle de cada payment por ID
    private final Map<String, List<MerchantOrder>> merchantOrdersPorPreference = new HashMap<>();
    private final Map<Long, Payment> paymentsPorId = new HashMap<>();
    private final Set<String> preferencesConError = new HashSet<>();

    private MockedConstruction<MerchantOrderClient> merchantOrderClients;
    private MockedConstruction<PaymentClient> paymentClients;
    private MockedConstruction<PreferenceClient> preferenceClients;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        visitanteRepository = mock(VisitanteRepository.class);
        EstadoPagoRepository estadoPagoRepository = mock(EstadoPagoRepository.class);
        self = mock(ReservaService.class);

        when(reservaRepository.findEstadoReservaByEstadoReservaNombre(EstadoReservaNombre.PAGADA)).thenReturn(Optional.of(estadoPagada));
        when(reservaRepository.findEstadoReservaByEstadoReservaNombre(EstadoReservaNombre.EXPIRADA)).thenReturn(Optional.of(estadoExpirada));
        when(estadoPagoRepository.findByNombre(EstadoPagoNombre.APROBADO))
                .thenReturn(Optional.of(new EstadoPago(EstadoPagoNombre.APROBADO, LocalDateTime.now(), null)));

        // Servicio real: opcionesDe no usa sus dependencias
        CuentaMercadoPagoService cuentaService = new CuentaMercadoPagoService(
                mock(EstablecimientoRepository.class), mock(CuentaMercadoPagoRepository.class),
                mock(MercadoPagoOAuthClient.class), mock(Cifrador.class));

        service = new ReservaService(reservaRepository, mock(ReservaMapper.class), mock(ActividadRepository.class),
                mock(ParametrosService.class), usuarioRepository, visitanteRepository,
                mock(TipoIdentificacionRepository.class), mock(EstrategiaPagoFactory.class), self,
                estadoPagoRepository, mock(NotificacionService.class), cuentaService, new ConciliadorPagoMP());

        merchantOrderClients = mockConstruction(MerchantOrderClient.class, (client, contexto) ->
                when(client.search(any(MPSearchRequest.class), any(MPRequestOptions.class))).thenAnswer(inv -> {
                    String preferenceId = (String) inv.<MPSearchRequest>getArgument(0).getFilters().get("preference_id");
                    if (preferencesConError.contains(preferenceId)) throw new MPException("timeout");
                    @SuppressWarnings("unchecked")
                    MPElementsResourcesPage<MerchantOrder> pagina = mock(MPElementsResourcesPage.class);
                    when(pagina.getElements()).thenReturn(merchantOrdersPorPreference.get(preferenceId));
                    return pagina;
                }));
        paymentClients = mockConstruction(PaymentClient.class, (client, contexto) ->
                when(client.get(anyLong(), any(MPRequestOptions.class))).thenAnswer(inv -> paymentsPorId.get(inv.<Long>getArgument(0))));
        preferenceClients = mockConstruction(PreferenceClient.class);
    }

    @AfterEach
    void tearDown() {
        merchantOrderClients.close();
        paymentClients.close();
        preferenceClients.close();
    }

    // ---------- Datos ----------

    private static CuentaMercadoPago cuentaVendedor() {
        CuentaMercadoPago cuenta = new CuentaMercadoPago();
        cuenta.setMpUserId(VENDEDOR);
        cuenta.setAccessToken(TOKEN_VENDEDOR);
        return cuenta;
    }

    /** Reserva pendiente de 10000 con 10% de comisión, pagada con la preference indicada. */
    private Reserva reservaPendiente(String preferenceId, CuentaMercadoPago cuenta) {
        Pago pago = new Pago();
        pago.setMetodoPago(MetodoPago.MERCADO_PAGO);
        pago.setIdCheckoutExterno(preferenceId);
        pago.setCuentaMercadoPago(cuenta);
        pago.setMontoTotal(new BigDecimal("10000.00"));
        pago.cambiarEstado(new EstadoPago(EstadoPagoNombre.PENDIENTE, LocalDateTime.now(), null), LocalDateTime.now());

        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID());
        reserva.setTotalReserva(new BigDecimal("10000.00"));
        reserva.setSubTotalComisionPropia(new BigDecimal("1000.00"));
        reserva.setSubTotalProductor(new BigDecimal("9000.00"));
        reserva.setFechaHoraExpiracion(LocalDateTime.now().plusMinutes(10));
        reserva.setPago(pago);
        return reserva;
    }

    private void pendientes(Reserva... reservas) {
        when(reservaRepository.findReservasPendientes(any())).thenReturn(List.of(reservas));
    }

    /** Registra en MP una merchant order de la preference con pagos {id, status}. */
    private void merchantOrder(String preferenceId, Object... idYStatus) {
        List<MerchantOrderPayment> pagos = new ArrayList<>();
        for (int i = 0; i < idYStatus.length; i += 2) {
            MerchantOrderPayment pago = mock(MerchantOrderPayment.class);
            when(pago.getId()).thenReturn((Long) idYStatus[i]);
            when(pago.getStatus()).thenReturn((String) idYStatus[i + 1]);
            pagos.add(pago);
        }
        MerchantOrder mo = mock(MerchantOrder.class);
        when(mo.getPayments()).thenReturn(pagos);
        merchantOrdersPorPreference.computeIfAbsent(preferenceId, k -> new ArrayList<>()).add(mo);
    }

    private static PaymentFeeDetail fee(String tipo, String feePayer, String monto) {
        PaymentFeeDetail fee = mock(PaymentFeeDetail.class);
        when(fee.getType()).thenReturn(tipo);
        when(fee.getFeePayer()).thenReturn(feePayer);
        when(fee.getAmount()).thenReturn(new BigDecimal(monto));
        return fee;
    }

    /** Registra en MP el detalle de un payment aprobado en ARS al vendedor. */
    private void payment(long id, String monto, Long collector) {
        // Los fees se arman antes: no se puede crear un mock dentro de otro when(...)
        List<PaymentFeeDetail> fees = List.of(
                fee("mercadopago_fee", "collector", "629.00"),
                fee("application_fee", "collector", "1000.00"));

        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(id);
        when(payment.getStatus()).thenReturn("approved");
        when(payment.getCurrencyId()).thenReturn("ARS");
        when(payment.getTransactionAmount()).thenReturn(new BigDecimal(monto));
        when(payment.getCollectorId()).thenReturn(collector);
        when(payment.getFeeDetails()).thenReturn(fees);
        paymentsPorId.put(id, payment);
    }

    private MerchantOrderClient merchantOrderClient() { return merchantOrderClients.constructed().get(0); }
    private PaymentClient paymentClient() { return paymentClients.constructed().get(0); }

    private void assertConfirmada(Reserva reserva) {
        assertEquals(EstadoPagoNombre.APROBADO, reserva.getPago().getEstadoActual().getEstadoPago().getNombre());
        assertNull(reserva.getFechaHoraExpiracion());
        verify(self).cambiarEstadoReservaYGuardar(eq(reserva), eq(estadoPagada), any());
    }

    private void assertNoConfirmada(Reserva reserva) {
        assertEquals(EstadoPagoNombre.PENDIENTE, reserva.getPago().getEstadoActual().getEstadoPago().getNombre());
        assertNull(reserva.getPago().getIdTransaccionExterna());
        assertNotNull(reserva.getFechaHoraExpiracion());
        verify(self, never()).cambiarEstadoReservaYGuardar(eq(reserva), any(), any());
    }

    // ---------- pagarReservas ----------

    @Test
    void confirmaElPagoAprobadoYRegistraElPaymentYLasComisiones() throws Exception {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);
        merchantOrder("pref-1", 987L, "approved");
        payment(987L, "10000", VENDEDOR);

        service.pagarReservas();

        assertConfirmada(reserva);
        assertEquals("987", reserva.getPago().getIdTransaccionExterna());
        assertEquals("pref-1", reserva.getPago().getIdCheckoutExterno());
        assertEquals(0, new BigDecimal("629.00").compareTo(reserva.getSubTotalComisionTransaccion()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(reserva.getSubTotalComisionPropia()));
        assertEquals(0, new BigDecimal("8371.00").compareTo(reserva.getSubTotalProductor()));
    }

    @Test
    void usaElTokenDelVendedorEnTodasLasLlamadasAMp() throws Exception {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);
        merchantOrder("pref-1", 987L, "approved");
        payment(987L, "10000", VENDEDOR);

        service.pagarReservas();

        ArgumentCaptor<MPSearchRequest> busqueda = ArgumentCaptor.forClass(MPSearchRequest.class);
        ArgumentCaptor<MPRequestOptions> opcionesBusqueda = ArgumentCaptor.forClass(MPRequestOptions.class);
        verify(merchantOrderClient()).search(busqueda.capture(), opcionesBusqueda.capture());
        assertEquals("pref-1", busqueda.getValue().getFilters().get("preference_id"));
        assertEquals(TOKEN_VENDEDOR, opcionesBusqueda.getValue().getAccessToken());

        ArgumentCaptor<MPRequestOptions> opcionesPayment = ArgumentCaptor.forClass(MPRequestOptions.class);
        verify(paymentClient()).get(eq(987L), opcionesPayment.capture());
        assertEquals(TOKEN_VENDEDOR, opcionesPayment.getValue().getAccessToken());

        // Se expira la preference pagada, también en la cuenta del vendedor
        ArgumentCaptor<PreferenceRequest> expiracion = ArgumentCaptor.forClass(PreferenceRequest.class);
        ArgumentCaptor<MPRequestOptions> opcionesExpiracion = ArgumentCaptor.forClass(MPRequestOptions.class);
        verify(preferenceClients.constructed().get(0)).update(eq("pref-1"), expiracion.capture(), opcionesExpiracion.capture());
        assertNotNull(expiracion.getValue().getExpirationDateTo());
        assertEquals(TOKEN_VENDEDOR, opcionesExpiracion.getValue().getAccessToken());
    }

    @Test
    void unPagoAnteriorAlSplitUsaElTokenGlobalDeAgrotours() throws Exception {
        Reserva reserva = reservaPendiente("pref-1", null);
        pendientes(reserva);
        merchantOrder("pref-1", 987L, "approved");
        payment(987L, "10000", 999L);    // Sin cuenta del vendedor no se valida el collector

        service.pagarReservas();

        assertConfirmada(reserva);
        ArgumentCaptor<MPRequestOptions> opciones = ArgumentCaptor.forClass(MPRequestOptions.class);
        verify(paymentClient()).get(eq(987L), opciones.capture());
        assertNull(opciones.getValue().getAccessToken(), "Sin token propio el SDK usa el de MercadoPagoConfig");
    }

    @Test
    void noConfirmaUnPagoQueNoConcilia() throws Exception {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);
        merchantOrder("pref-1", 987L, "approved");
        payment(987L, "9999.99", VENDEDOR);

        service.pagarReservas();

        assertNoConfirmada(reserva);
        assertTrue(preferenceClients.constructed().isEmpty(), "No se expira la preference de una reserva no confirmada");
    }

    @Test
    void noConfirmaUnPagoAcreditadoAOtroVendedor() {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);
        merchantOrder("pref-1", 987L, "approved");
        payment(987L, "10000", 999L);

        service.pagarReservas();

        assertNoConfirmada(reserva);
    }

    @Test
    void sinPagosAprobadosNoConsultaElPaymentNiConfirma() throws Exception {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);
        merchantOrder("pref-1", 1L, "rejected", 2L, "in_process");

        service.pagarReservas();

        assertNoConfirmada(reserva);
        verify(paymentClient(), never()).get(anyLong(), any(MPRequestOptions.class));
    }

    @Test
    void sinMerchantOrdersNoHaceNada() throws Exception {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);

        service.pagarReservas();

        assertNoConfirmada(reserva);
        verify(paymentClient(), never()).get(anyLong(), any(MPRequestOptions.class));
    }

    @Test
    void conVariosPagosAprobadosConciliaSoloElPrimero() throws Exception {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);
        merchantOrder("pref-1", 987L, "approved");
        merchantOrder("pref-1", 988L, "approved");
        payment(987L, "10000", VENDEDOR);
        payment(988L, "10000", VENDEDOR);

        service.pagarReservas();

        assertConfirmada(reserva);
        assertEquals("987", reserva.getPago().getIdTransaccionExterna());
        verify(paymentClient()).get(eq(987L), any(MPRequestOptions.class));
        verify(paymentClient(), never()).get(eq(988L), any(MPRequestOptions.class));
        verify(self, times(1)).cambiarEstadoReservaYGuardar(eq(reserva), any(), any());
    }

    @Test
    void unErrorDeMpEnUnaReservaNoFrenaLasDemas() {
        Reserva conError = reservaPendiente("pref-error", cuentaVendedor());
        Reserva pagada = reservaPendiente("pref-ok", cuentaVendedor());
        pendientes(conError, pagada);
        preferencesConError.add("pref-error");
        merchantOrder("pref-ok", 987L, "approved");
        payment(987L, "10000", VENDEDOR);

        service.pagarReservas();

        assertNoConfirmada(conError);
        assertConfirmada(pagada);
    }

    @Test
    void siFallaLaExpiracionDeLaPreferenceLaReservaQuedaConfirmada() throws Exception {
        preferenceClients.close();
        preferenceClients = mockConstruction(PreferenceClient.class, (client, contexto) ->
                when(client.update(anyString(), any(PreferenceRequest.class), any(MPRequestOptions.class)))
                        .thenThrow(new MPException("timeout")));

        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);
        merchantOrder("pref-1", 987L, "approved");
        payment(987L, "10000", VENDEDOR);

        service.pagarReservas();

        assertConfirmada(reserva);
        assertEquals("987", reserva.getPago().getIdTransaccionExterna());
    }

    // ---------- handleCancelarPago ----------

    @Test
    void cancelarElPagoExpiraLaPreferenceConElTokenDelVendedor() throws Exception {
        Usuario usuario = mock(Usuario.class);
        Visitante visitante = mock(Visitante.class);
        when(usuarioRepository.findActiveByEmail("visitante@test.com")).thenReturn(Optional.of(usuario));
        when(visitanteRepository.findByUsuario(usuario)).thenReturn(Optional.of(visitante));

        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        reserva.setVisitante(visitante);
        when(reservaRepository.findByPagoWithIdCheckoutExterno("pref-1")).thenReturn(Optional.of(reserva));

        service.handleCancelarPago("pref-1", "visitante@test.com");

        verify(self).cambiarEstadoReservaYGuardar(eq(reserva), eq(estadoExpirada), any());
        ArgumentCaptor<MPRequestOptions> opciones = ArgumentCaptor.forClass(MPRequestOptions.class);
        verify(preferenceClients.constructed().get(0)).update(eq("pref-1"), any(PreferenceRequest.class), opciones.capture());
        assertEquals(TOKEN_VENDEDOR, opciones.getValue().getAccessToken());
    }
}
