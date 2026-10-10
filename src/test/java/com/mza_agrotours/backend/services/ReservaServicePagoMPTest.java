package com.mza_agrotours.backend.services;

import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentRefundClient;
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
import com.mercadopago.resources.payment.PaymentRefund;
import com.mza_agrotours.backend.clients.mercadopago.MercadoPagoOAuthClient;
import com.mza_agrotours.backend.dtos.pago.ResultadoReembolsoDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.EstadoReembolso;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.EstadoPagoNombre;
import com.mza_agrotours.backend.enums.EstadoReembolsoNombre;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.enums.MetodoPago;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
import com.mza_agrotours.backend.mappers.reserva.ReservaMapper;
import com.mza_agrotours.backend.repositories.*;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoPagoRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoReembolsoRepository;
import com.mza_agrotours.backend.repositories.pago.PagoRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoRepository;
import com.mza_agrotours.backend.security.cifrado.Cifrador;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import com.mza_agrotours.backend.services.pago.ConciliadorPagoMP;
import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService;
import com.mza_agrotours.backend.services.pago.EstrategiaPagoFactory;
import com.mza_agrotours.backend.services.pago.PagoMPStrategy;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * Llamadas a Mercado Pago de ReservaService (a través de {@link PagoMPStrategy}) con el split de marketplace:
 * confirmación de pagos por el scheduler (conciliación), reembolso de pagos que no concilian, confirmación de
 * reembolsos y expiración de preferences, siempre con el token de la cuenta del vendedor que creó la preference.
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
    private ReembolsoRepository reembolsoRepository;
    private NotificacionService notificacionService;

    private final EstadoReserva estadoPagada = new EstadoReserva(EstadoReservaNombre.PAGADA);
    private final EstadoReserva estadoExpirada = new EstadoReserva(EstadoReservaNombre.EXPIRADA);

    // Respuestas de MP por preference: merchant orders con sus pagos, y el detalle de cada payment por ID
    private final Map<String, List<MerchantOrder>> merchantOrdersPorPreference = new HashMap<>();
    private final Map<Long, Payment> paymentsPorId = new HashMap<>();
    private final Set<String> preferencesConError = new HashSet<>();
    private String estadoRefundEnMp = "approved";   // Estado que devuelve MP al consultar un refund

    private MockedConstruction<MerchantOrderClient> merchantOrderClients;
    private MockedConstruction<PaymentClient> paymentClients;
    private MockedConstruction<PreferenceClient> preferenceClients;
    private MockedConstruction<PaymentRefundClient> refundClients;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        visitanteRepository = mock(VisitanteRepository.class);
        reembolsoRepository = mock(ReembolsoRepository.class);
        notificacionService = mock(NotificacionService.class);
        EstadoPagoRepository estadoPagoRepository = mock(EstadoPagoRepository.class);
        EstadoReembolsoRepository estadoReembolsoRepository = mock(EstadoReembolsoRepository.class);
        self = mock(ReservaService.class);

        when(reservaRepository.findEstadoReservaByEstadoReservaNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoReserva(inv.<EstadoReservaNombre>getArgument(0))));
        when(reservaRepository.findEstadoReservaByEstadoReservaNombre(EstadoReservaNombre.PAGADA)).thenReturn(Optional.of(estadoPagada));
        when(reservaRepository.findEstadoReservaByEstadoReservaNombre(EstadoReservaNombre.EXPIRADA)).thenReturn(Optional.of(estadoExpirada));
        when(estadoPagoRepository.findByNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoPago(inv.<EstadoPagoNombre>getArgument(0), LocalDateTime.now(), null)));
        when(estadoReembolsoRepository.findByNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoReembolso(LocalDateTime.now(), null, inv.<EstadoReembolsoNombre>getArgument(0))));

        // Servicio real: opcionesDe no usa sus dependencias
        CuentaMercadoPagoService cuentaService = new CuentaMercadoPagoService(
                mock(EstablecimientoRepository.class), mock(CuentaMercadoPagoRepository.class),
                mock(MercadoPagoOAuthClient.class), mock(Cifrador.class));

        // Estrategia real: es la que habla con MP; ReservaService solo la obtiene de la factory
        PagoMPStrategy estrategiaMP = new PagoMPStrategy(mock(PagoRepository.class), mock(ParametrosService.class),
                cuentaService, new ConciliadorPagoMP());
        EstrategiaPagoFactory estrategiaPagoFactory = mock(EstrategiaPagoFactory.class);
        when(estrategiaPagoFactory.get(MetodoPago.MERCADO_PAGO)).thenReturn(estrategiaMP);

        service = new ReservaService(reservaRepository, mock(ReservaMapper.class), mock(ActividadRepository.class),
                mock(ParametrosService.class), usuarioRepository, visitanteRepository,
                mock(TipoIdentificacionRepository.class), estrategiaPagoFactory, self,
                estadoPagoRepository, notificacionService, estadoReembolsoRepository, reembolsoRepository);

        // El paso 1 del reembolso de un pago no conciliado corre en su propia transacción vía self: se ejecuta el real
        when(self.registrarPagoNoConciliable(any(), any(), any(), any())).thenAnswer(inv -> service.registrarPagoNoConciliable(
                inv.getArgument(0), inv.getArgument(1), inv.getArgument(2), inv.getArgument(3)));

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
        refundClients = mockConstruction(PaymentRefundClient.class, (client, contexto) -> {
            PaymentRefund refund = mock(PaymentRefund.class);
            when(refund.getId()).thenReturn(555L);
            when(refund.getStatus()).thenAnswer(inv -> estadoRefundEnMp);
            when(client.refund(anyLong(), any(MPRequestOptions.class))).thenReturn(refund);
            when(client.get(anyLong(), anyLong(), any(MPRequestOptions.class))).thenReturn(refund);
        });
    }

    @AfterEach
    void tearDown() {
        merchantOrderClients.close();
        paymentClients.close();
        preferenceClients.close();
        refundClients.close();
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

        // Datos que usa la notificación al visitante
        Visitante visitante = mock(Visitante.class);
        when(visitante.getUsuario()).thenReturn(mock(Usuario.class));
        Actividad actividad = mock(Actividad.class);
        when(actividad.getNombre()).thenReturn("Cosecha");
        ActividadDia dia = mock(ActividadDia.class);
        when(dia.getFechaHoraInicio()).thenReturn(LocalDateTime.now().plusDays(10));

        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID());
        reserva.setVisitante(visitante);
        reserva.setActividad(actividad);
        reserva.setActividadDia(dia);
        reserva.cambiarEstado(new EstadoReserva(EstadoReservaNombre.PENDIENTE), LocalDateTime.now());
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
    void unPagoQueNoConciliaSeReembolsaYLaReservaQuedaReembolsadaPorPagoNoValido() throws Exception {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);
        merchantOrder("pref-1", 987L, "approved");
        payment(987L, "9999.99", VENDEDOR);

        service.pagarReservas();

        // No se confirma: la reserva pasa a su estado final y libera el cupo
        verify(self, never()).cambiarEstadoReservaYGuardar(any(), any(), any());
        assertEquals(EstadoReservaNombre.REEMBOLSO_NO_CONCILIACION, reserva.getEstadoActual().getEstadoReserva().getNombre());
        assertNull(reserva.getFechaHoraExpiracion());

        // El pago guarda el payment (necesario para reembolsar) y queda aprobado hasta confirmarse el reembolso
        assertEquals("987", reserva.getPago().getIdTransaccionExterna());
        assertEquals(EstadoPagoNombre.APROBADO, reserva.getPago().getEstadoActual().getEstadoPago().getNombre());

        // Se guarda el reembolso "En proceso" por lo realmente pagado, antes de pedirlo
        ArgumentCaptor<Reembolso> reembolso = ArgumentCaptor.forClass(Reembolso.class);
        verify(reembolsoRepository).save(reembolso.capture());
        assertEquals(EstadoReembolsoNombre.EN_PROCESO, reembolso.getValue().getEstadoActual().getEstadoReembolso().getNombre());
        assertEquals(new BigDecimal("9999.99"), reembolso.getValue().getMontoReembolso());
        assertSame(reserva, reembolso.getValue().getReserva());

        // Se expira la preference y se pide el reembolso, ambos con el token del vendedor
        ArgumentCaptor<MPRequestOptions> opcionesExpiracion = ArgumentCaptor.forClass(MPRequestOptions.class);
        verify(preferenceClients.constructed().get(0)).update(eq("pref-1"), any(PreferenceRequest.class), opcionesExpiracion.capture());
        assertEquals(TOKEN_VENDEDOR, opcionesExpiracion.getValue().getAccessToken());

        ArgumentCaptor<MPRequestOptions> opcionesReembolso = ArgumentCaptor.forClass(MPRequestOptions.class);
        verify(refundClients.constructed().get(0)).refund(eq(987L), opcionesReembolso.capture());
        assertEquals(TOKEN_VENDEDOR, opcionesReembolso.getValue().getAccessToken());

        // Se guarda la respuesta de MP y se avisa al visitante
        verify(self).registrarPedidoReembolso(any(), argThat(ResultadoReembolsoDTO::aceptado));
        verify(notificacionService).crearNotificacion(eq(reserva.getVisitante().getUsuario()),
                eq(TipoNotificacionNombre.RESERVA_REEMBOLSADA_PAGO_NO_VALIDO), isNull(), anyString(),
                eq("Cosecha"), anyString(), eq("9999.99"));
    }

    @Test
    void unPagoAcreditadoAOtroVendedorTambienSeReembolsa() throws Exception {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);
        merchantOrder("pref-1", 987L, "approved");
        payment(987L, "10000", 999L);

        service.pagarReservas();

        verify(self, never()).cambiarEstadoReservaYGuardar(any(), any(), any());
        assertEquals(EstadoReservaNombre.REEMBOLSO_NO_CONCILIACION, reserva.getEstadoActual().getEstadoReserva().getNombre());
        verify(refundClients.constructed().get(0)).refund(eq(987L), any(MPRequestOptions.class));
    }

    @Test
    void siNoSeSabeSiMpHizoElReembolsoQuedaEnProcesoParaLaTareaProgramada() throws Exception {
        refundClients.close();
        refundClients = mockConstruction(PaymentRefundClient.class, (client, contexto) ->
                when(client.refund(anyLong(), any(MPRequestOptions.class))).thenThrow(new MPException("timeout")));

        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);
        merchantOrder("pref-1", 987L, "approved");
        payment(987L, "9999.99", VENDEDOR);

        service.pagarReservas();

        // La reserva y el reembolso ya quedaron guardados; no se guarda respuesta, confirmarReembolsos lo busca en MP
        assertEquals(EstadoReservaNombre.REEMBOLSO_NO_CONCILIACION, reserva.getEstadoActual().getEstadoReserva().getNombre());
        verify(reembolsoRepository).save(any(Reembolso.class));
        verify(self, never()).registrarPedidoReembolso(any(), any());
    }

    // ---------- confirmarReembolsos ----------

    /** Reembolso "En proceso" ya pedido a MP (refund 555 del payment 987) de una reserva en el estado indicado. */
    private Reembolso reembolsoEnProceso(EstadoReservaNombre estadoReserva) {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        reserva.getPago().setIdTransaccionExterna("987");
        reserva.getPago().cambiarEstado(new EstadoPago(EstadoPagoNombre.APROBADO, LocalDateTime.now(), null), LocalDateTime.now());
        reserva.cambiarEstado(new EstadoReserva(estadoReserva), LocalDateTime.now());

        Reembolso reembolso = new Reembolso();
        reembolso.setReserva(reserva);
        reembolso.setMontoReembolso(new BigDecimal("9999.99"));
        reembolso.setFechaHoraPedido(LocalDateTime.now().minusMinutes(10));
        reembolso.setIdReembolsoExterno("555");
        reembolso.cambiarEstado(new EstadoReembolso(LocalDateTime.now(), null, EstadoReembolsoNombre.EN_PROCESO), LocalDateTime.now());
        when(reembolsoRepository.findReembolsosEnProceso()).thenReturn(List.of(reembolso));
        return reembolso;
    }

    @Test
    void alConfirmarseElReembolsoDeUnPagoNoConciliadoLaReservaNoCambia() throws Exception {
        Reembolso reembolso = reembolsoEnProceso(EstadoReservaNombre.REEMBOLSO_NO_CONCILIACION);
        estadoRefundEnMp = "approved";

        service.confirmarReembolsos();

        Reserva reserva = reembolso.getReserva();
        assertEquals(EstadoReembolsoNombre.REEMBOLSADO_PRODUCTOR, reembolso.getEstadoActual().getEstadoReembolso().getNombre());
        assertEquals(EstadoPagoNombre.REEMBOLSADO, reserva.getPago().getEstadoActual().getEstadoPago().getNombre());
        assertEquals(EstadoReservaNombre.REEMBOLSO_NO_CONCILIACION, reserva.getEstadoActual().getEstadoReserva().getNombre());
        verify(self).guardarReembolso(reembolso);

        ArgumentCaptor<MPRequestOptions> opciones = ArgumentCaptor.forClass(MPRequestOptions.class);
        verify(refundClients.constructed().get(0)).get(eq(987L), eq(555L), opciones.capture());
        assertEquals(TOKEN_VENDEDOR, opciones.getValue().getAccessToken());
    }

    @Test
    void unReembolsoRechazadoDeUnPagoNoConciliadoPasaAPedido() {
        Reembolso reembolso = reembolsoEnProceso(EstadoReservaNombre.REEMBOLSO_NO_CONCILIACION);
        estadoRefundEnMp = "rejected";

        service.confirmarReembolsos();

        Reserva reserva = reembolso.getReserva();
        assertEquals(EstadoReembolsoNombre.PEDIDO, reembolso.getEstadoActual().getEstadoReembolso().getNombre());
        assertEquals(EstadoPagoNombre.APROBADO, reserva.getPago().getEstadoActual().getEstadoPago().getNombre());
        assertEquals(EstadoReservaNombre.REEMBOLSO_NO_CONCILIACION, reserva.getEstadoActual().getEstadoReserva().getNombre());
        verify(self).guardarReembolso(reembolso);
    }

    @Test
    void alConfirmarseElReembolsoDeUnaCancelacionLaReservaQuedaCanceladaConReembolso() {
        Reembolso reembolso = reembolsoEnProceso(EstadoReservaNombre.CANCELADA_REEMBOLSO_PENDIENTE);
        estadoRefundEnMp = "approved";

        service.confirmarReembolsos();

        assertEquals(EstadoReservaNombre.CANCELADA_CON_REEMBOLSO,
                reembolso.getReserva().getEstadoActual().getEstadoReserva().getNombre());
    }

    @Test
    void sinPagosAprobadosNoConsultaElPaymentNiConfirma() throws Exception {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);
        merchantOrder("pref-1", 1L, "rejected", 2L, "in_process");

        service.pagarReservas();

        assertNoConfirmada(reserva);
        assertTrue(paymentClients.constructed().isEmpty(), "No se consulta el detalle de ningún payment");
    }

    @Test
    void sinMerchantOrdersNoHaceNada() throws Exception {
        Reserva reserva = reservaPendiente("pref-1", cuentaVendedor());
        pendientes(reserva);

        service.pagarReservas();

        assertNoConfirmada(reserva);
        assertTrue(paymentClients.constructed().isEmpty(), "No se consulta el detalle de ningún payment");
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
