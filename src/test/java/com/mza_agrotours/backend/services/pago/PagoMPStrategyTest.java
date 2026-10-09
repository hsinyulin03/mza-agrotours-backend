package com.mza_agrotours.backend.services.pago;

import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.resources.preference.Preference;
import com.mza_agrotours.backend.clients.mercadopago.MercadoPagoOAuthClient;
import com.mza_agrotours.backend.dtos.reservas.PagoStrategyDTO;
import com.mza_agrotours.backend.entities.Parametros;
import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.TipoIdentificacionNombre;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.EstadoPagoNombre;
import com.mza_agrotours.backend.enums.MetodoPago;
import com.mza_agrotours.backend.exceptions.pago.EstablecimientoSinCuentaMercadoPagoException;
import com.mza_agrotours.backend.repositories.CuentaMercadoPagoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.pago.PagoRepository;
import com.mza_agrotours.backend.security.cifrado.Cifrador;
import com.mza_agrotours.backend.services.ParametrosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Creación de la preference de Checkout Pro en nombre del vendedor (split de marketplace).
 * El {@link PreferenceClient} se instancia dentro del método, así que se intercepta con {@code mockConstruction}.
 */
class PagoMPStrategyTest {

    private static final String TOKEN_VENDEDOR = "APP_USR-token-del-vendedor";

    private PagoMPStrategy strategy;
    private Establecimiento establecimiento;
    private Reserva reserva;
    private Parametros parametros;

    @BeforeEach
    void setUp() {
        PagoRepository pagoRepository = mock(PagoRepository.class);
        when(pagoRepository.findEstadoPagoByEstadoPagoNombre(EstadoPagoNombre.PENDIENTE))
                .thenReturn(Optional.of(new EstadoPago(EstadoPagoNombre.PENDIENTE, LocalDateTime.now(), null)));

        parametros = mock(Parametros.class);
        when(parametros.getPorcentajeComision()).thenReturn(new BigDecimal("0.10"));
        ParametrosService parametrosService = mock(ParametrosService.class);
        when(parametrosService.getInstance()).thenReturn(parametros);

        // Servicio real: getCuentaParaCobrar y opcionesDe no usan sus dependencias
        CuentaMercadoPagoService cuentaService = new CuentaMercadoPagoService(
                mock(EstablecimientoRepository.class), mock(CuentaMercadoPagoRepository.class),
                mock(MercadoPagoOAuthClient.class), mock(Cifrador.class));

        strategy = new PagoMPStrategy(pagoRepository, parametrosService, cuentaService);

        establecimiento = new Establecimiento();
        establecimiento.setId(UUID.randomUUID());

        reserva = reservaDe(establecimiento, new BigDecimal("10000.00"));
    }

    private static Reserva reservaDe(Establecimiento establecimiento, BigDecimal total) {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getNombre()).thenReturn("Visitante");
        when(usuario.getEmail()).thenReturn("visitante@test.com");
        when(usuario.getIdentificacion()).thenReturn("30111222");
        TipoIdentificacion dni = new TipoIdentificacion();
        dni.setNombre(TipoIdentificacionNombre.DNI);
        when(usuario.getTipoIdentificacion()).thenReturn(dni);

        Visitante visitante = mock(Visitante.class);
        when(visitante.getUsuario()).thenReturn(usuario);

        Actividad actividad = mock(Actividad.class);
        when(actividad.getNombre()).thenReturn("Cosecha");
        when(actividad.getEstablecimiento()).thenReturn(establecimiento);

        ActividadDia dia = mock(ActividadDia.class);
        when(dia.getId()).thenReturn(UUID.randomUUID());

        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID());
        reserva.setTotalReserva(total);
        reserva.setFechaHoraExpiracion(LocalDateTime.now().plusMinutes(15));
        reserva.setVisitante(visitante);
        reserva.setActividad(actividad);
        reserva.setActividadDia(dia);
        return reserva;
    }

    private CuentaMercadoPago vincularCuenta(LocalDateTime vencimientoToken) {
        CuentaMercadoPago cuenta = new CuentaMercadoPago();
        cuenta.setVinculadaPor(new Productor());
        cuenta.setMpUserId(123456L);
        cuenta.setAccessToken(TOKEN_VENDEDOR);
        cuenta.setRefreshToken("TG-refresh");
        cuenta.setFechaHoraExpiracionToken(vencimientoToken);
        establecimiento.vincularCuentaMercadoPago(cuenta, LocalDateTime.now());
        return cuenta;
    }

    /** Intercepta el PreferenceClient: create devuelve una preference con el ID indicado. */
    private static MockedConstruction<PreferenceClient> preferenceClientQueDevuelve(String preferenceId) {
        Preference preference = mock(Preference.class);
        when(preference.getId()).thenReturn(preferenceId);
        return mockConstruction(PreferenceClient.class, (client, contexto) ->
                when(client.create(any(PreferenceRequest.class), any(MPRequestOptions.class))).thenReturn(preference));
    }

    @Test
    void creaLaPreferenceConElTokenDelVendedorYLaComisionComoMarketplaceFee() throws Exception {
        CuentaMercadoPago cuenta = vincularCuenta(LocalDateTime.now().plusDays(100));

        try (MockedConstruction<PreferenceClient> clientes = preferenceClientQueDevuelve("pref-123")) {
            PagoStrategyDTO resultado = strategy.procesarPago(reserva);

            ArgumentCaptor<PreferenceRequest> request = ArgumentCaptor.forClass(PreferenceRequest.class);
            ArgumentCaptor<MPRequestOptions> opciones = ArgumentCaptor.forClass(MPRequestOptions.class);
            verify(clientes.constructed().get(0)).create(request.capture(), opciones.capture());

            assertEquals(TOKEN_VENDEDOR, opciones.getValue().getAccessToken());
            assertEquals(0, new BigDecimal("1000.00").compareTo(request.getValue().getMarketplaceFee()));
            assertEquals(0, new BigDecimal("10000.00").compareTo(request.getValue().getItems().get(0).getUnitPrice()));
            assertEquals(reserva.getId().toString(), request.getValue().getExternalReference());

            Pago pago = resultado.pago();
            assertEquals("pref-123", resultado.preferenceID());
            assertEquals("pref-123", pago.getIdCheckoutExterno());
            assertNull(pago.getIdTransaccionExterna(), "El ID del payment se completa recién al conciliar");
            assertSame(cuenta, pago.getCuentaMercadoPago());
            assertEquals(MetodoPago.MERCADO_PAGO, pago.getMetodoPago());
            assertEquals(EstadoPagoNombre.PENDIENTE, pago.getEstadoActual().getEstadoPago().getNombre());
            assertSame(pago, reserva.getPago());
        }
    }

    @Test
    void registraLaComisionEstimadaEnLaReserva() {
        vincularCuenta(LocalDateTime.now().plusDays(100));

        try (MockedConstruction<PreferenceClient> ignored = preferenceClientQueDevuelve("pref-123")) {
            strategy.procesarPago(reserva);
        }

        assertEquals(0, new BigDecimal("1000.00").compareTo(reserva.getSubTotalComisionPropia()));
        assertEquals(0, new BigDecimal("9000.00").compareTo(reserva.getSubTotalProductor()));
        assertEquals(0, BigDecimal.ZERO.compareTo(reserva.getSubTotalComisionTransaccion()));
    }

    @Test
    void redondeaLaComisionADosDecimalesHalfUp() throws Exception {
        vincularCuenta(LocalDateTime.now().plusDays(100));
        Reserva conCentavos = reservaDe(establecimiento, new BigDecimal("3333.35"));    // 10% = 333.335

        try (MockedConstruction<PreferenceClient> clientes = preferenceClientQueDevuelve("pref-123")) {
            strategy.procesarPago(conCentavos);

            ArgumentCaptor<PreferenceRequest> request = ArgumentCaptor.forClass(PreferenceRequest.class);
            verify(clientes.constructed().get(0)).create(request.capture(), any(MPRequestOptions.class));
            assertEquals(new BigDecimal("333.34"), request.getValue().getMarketplaceFee());
        }
        assertEquals(new BigDecimal("333.34"), conCentavos.getSubTotalComisionPropia());
    }

    @Test
    void sinCuentaVinculadaRechazaSinLlamarAMp() {
        try (MockedConstruction<PreferenceClient> clientes = preferenceClientQueDevuelve("pref-123")) {
            assertThrows(EstablecimientoSinCuentaMercadoPagoException.class, () -> strategy.procesarPago(reserva));
            assertTrue(clientes.constructed().isEmpty());
        }
        assertNull(reserva.getPago());
    }

    @Test
    void conElTokenDelVendedorVencidoRechazaSinLlamarAMp() {
        vincularCuenta(LocalDateTime.now().minusMinutes(1));

        try (MockedConstruction<PreferenceClient> clientes = preferenceClientQueDevuelve("pref-123")) {
            assertThrows(EstablecimientoSinCuentaMercadoPagoException.class, () -> strategy.procesarPago(reserva));
            assertTrue(clientes.constructed().isEmpty());
        }
    }

    @Test
    void conLaCuentaDadaDeBajaRechazaSinLlamarAMp() {
        vincularCuenta(LocalDateTime.now().plusDays(100)).setFechaHoraBaja(LocalDateTime.now());

        try (MockedConstruction<PreferenceClient> clientes = preferenceClientQueDevuelve("pref-123")) {
            assertThrows(EstablecimientoSinCuentaMercadoPagoException.class, () -> strategy.procesarPago(reserva));
            assertTrue(clientes.constructed().isEmpty());
        }
    }
}