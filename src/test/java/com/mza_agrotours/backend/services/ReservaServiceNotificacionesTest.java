package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.pago.ResultadoReembolsoDTO;
import com.mza_agrotours.backend.dtos.reservas.PagoStrategyDTO;
import com.mza_agrotours.backend.dtos.reservas.RealizarReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.RealizarReservaDetalleDTO;
import com.mza_agrotours.backend.entities.Parametros;
import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.TipoIdentificacionNombre;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.actividad.ActividadRangoEtario;
import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.EstadoReembolso;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.entities.reservas.ReservaDetalle;
import com.mza_agrotours.backend.enums.*;
import com.mza_agrotours.backend.mappers.reserva.ReservaMapper;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.TipoIdentificacionRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import com.mza_agrotours.backend.repositories.VisitanteRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoPagoRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoReembolsoRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoSistemaPagoRepository;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import com.mza_agrotours.backend.services.pago.EstrategiaPago;
import com.mza_agrotours.backend.services.pago.EstrategiaPagoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * Notificaciones al visitante de ReservaService ante la creación y los cambios de estado de sus reservas, pagos
 * y reembolsos. La pasarela de pago se reemplaza por una estrategia mockeada.
 */
class ReservaServiceNotificacionesTest {

    private static final String EMAIL = "visitante@test.com";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ReservaRepository reservaRepository;
    private ReembolsoRepository reembolsoRepository;
    private TipoIdentificacionRepository tipoIdentificacionRepository;
    private ActividadRepository actividadRepository;
    private ReservaMapper reservaMapper;
    private NotificacionService notificacionService;
    private EstrategiaPago estrategiaPago;
    private ReservaService self;
    private ReservaService service;

    private final Parametros parametros = new Parametros();
    private final Usuario usuario = mock(Usuario.class);
    private final Visitante visitante = mock(Visitante.class);

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        reembolsoRepository = mock(ReembolsoRepository.class);
        tipoIdentificacionRepository = mock(TipoIdentificacionRepository.class);
        actividadRepository = mock(ActividadRepository.class);
        reservaMapper = mock(ReservaMapper.class);
        notificacionService = mock(NotificacionService.class);
        estrategiaPago = mock(EstrategiaPago.class);
        self = mock(ReservaService.class);

        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VisitanteRepository visitanteRepository = mock(VisitanteRepository.class);
        when(usuarioRepository.findActiveByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(visitanteRepository.findByUsuario(usuario)).thenReturn(Optional.of(visitante));
        when(visitante.getId()).thenReturn(UUID.randomUUID());
        when(visitante.getUsuario()).thenReturn(usuario);

        ParametrosService parametrosService = mock(ParametrosService.class);
        when(parametrosService.getInstance()).thenReturn(parametros);
        parametros.setDiasMinReembolso(5);
        parametros.setTtlReserva(15);

        EstadoPagoRepository estadoPagoRepository = mock(EstadoPagoRepository.class);
        EstadoReembolsoRepository estadoReembolsoRepository = mock(EstadoReembolsoRepository.class);
        when(reservaRepository.findEstadoReservaByEstadoReservaNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoReserva(inv.<EstadoReservaNombre>getArgument(0))));
        when(estadoPagoRepository.findByNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoPago(inv.<EstadoPagoNombre>getArgument(0), LocalDateTime.now(), null)));
        when(estadoReembolsoRepository.findByNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoReembolso(LocalDateTime.now(), null, inv.<EstadoReembolsoNombre>getArgument(0))));

        EstrategiaPagoFactory estrategiaPagoFactory = mock(EstrategiaPagoFactory.class);
        when(estrategiaPagoFactory.get(any())).thenReturn(estrategiaPago);

        service = new ReservaService(reservaRepository, reservaMapper, actividadRepository, parametrosService,
                usuarioRepository, visitanteRepository, tipoIdentificacionRepository, estrategiaPagoFactory, self,
                estadoPagoRepository, notificacionService, estadoReembolsoRepository, reembolsoRepository,
                mock(ReembolsoSistemaPagoRepository.class), mock(AdministradorSistemasRepository.class));
    }

    // ---------- Datos ----------

    private static Pago pago(MetodoPago metodo, EstadoPagoNombre estado) {
        Pago pago = new Pago();
        pago.setMetodoPago(metodo);
        pago.setMontoTotal(new BigDecimal("10000.00"));
        pago.cambiarEstado(new EstadoPago(estado, LocalDateTime.now(), null), LocalDateTime.now());
        return pago;
    }

    private static ActividadDia dia(UUID id, LocalDateTime inicio) {
        ActividadDia dia = mock(ActividadDia.class, RETURNS_DEEP_STUBS);
        when(dia.getId()).thenReturn(id);
        when(dia.getFechaHoraInicio()).thenReturn(inicio);
        when(dia.getFechaHoraFin()).thenReturn(inicio.plusHours(2));
        when(dia.getCuposMax()).thenReturn(10);
        when(dia.getEstadoActual().getEstado().getNombre()).thenReturn(EstadoActividadDiaNombre.ACTIVA);
        return dia;
    }

    /** Reserva "Pagada" del visitante, de la actividad "Cosecha" en la fecha indicada. */
    private Reserva reservaPagada(MetodoPago metodo, LocalDateTime inicio) {
        Actividad actividad = mock(Actividad.class);
        when(actividad.getNombre()).thenReturn("Cosecha");

        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID());
        reserva.setVisitante(visitante);
        reserva.setActividad(actividad);
        reserva.setActividadDia(dia(UUID.randomUUID(), inicio));
        reserva.setTotalReserva(new BigDecimal("10000.00"));
        reserva.setPago(pago(metodo, EstadoPagoNombre.APROBADO));
        reserva.cambiarEstado(new EstadoReserva(EstadoReservaNombre.PAGADA), LocalDateTime.now());
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        return reserva;
    }

    private Reembolso reembolso(Reserva reserva) {
        Reembolso reembolso = new Reembolso();
        reembolso.setId(UUID.randomUUID());
        reembolso.setReserva(reserva);
        reembolso.setMontoReembolso(new BigDecimal("10000.00"));
        reembolso.cambiarEstado(new EstadoReembolso(LocalDateTime.now(), null, EstadoReembolsoNombre.EN_PROCESO), LocalDateTime.now());
        when(reembolsoRepository.findById(reembolso.getId())).thenReturn(Optional.of(reembolso));
        return reembolso;
    }

    private static String fecha(Reserva reserva) {
        return reserva.getActividadDia().getFechaHoraInicio().format(FORMATO);
    }

    private static String link(Reserva reserva) {
        return "/mis-reservas/" + reserva.getId();
    }

    /** Prepara una actividad publicada con un día y cupo libre, cuyo pago devuelve la estrategia en el estado indicado. */
    private RealizarReservaDTO prepararInicioDeReserva(LocalDateTime inicio, EstadoPagoNombre estadoPago) {
        UUID diaId = UUID.randomUUID();
        ActividadDia dia = dia(diaId, inicio);

        ActividadRangoEtario rango = new ActividadRangoEtario();
        rango.setEdadMinima(0);
        rango.setEdadMaxima(120);
        rango.setPrecio(new BigDecimal("10000.00"));

        Actividad actividad = mock(Actividad.class, RETURNS_DEEP_STUBS);
        when(actividad.getNombre()).thenReturn("Cosecha");
        when(actividad.getFechaHoraBaja()).thenReturn(null);
        when(actividad.getEstado().getNombre()).thenReturn(EstadoActividadNombre.PUBLICADO);
        when(actividad.getEstablecimiento().admiteReservas(any())).thenReturn(true);
        when(actividad.getActividadRangoEtarios()).thenReturn(List.of(rango));
        when(actividad.getActividadesDias()).thenReturn(List.of(dia));

        when(reservaRepository.findByVisitanteIdAndActividadDiaId(any(), eq(diaId))).thenReturn(Optional.empty());
        when(actividadRepository.getActividadByDiaActividadId(diaId)).thenReturn(Optional.of(actividad));
        when(reservaRepository.getCuposReservadosActividadDia(diaId)).thenReturn(0L);
        when(tipoIdentificacionRepository.findByNombre(TipoIdentificacionNombre.DNI)).thenReturn(Optional.of(new TipoIdentificacion()));
        when(reservaMapper.DTOtoReservaDetalle(any(), any(), any(), any())).thenReturn(mock(ReservaDetalle.class));
        when(estrategiaPago.procesarPago(any())).thenReturn(new PagoStrategyDTO(pago(MetodoPago.MERCADO_PAGO, estadoPago), "pref-1"));

        return new RealizarReservaDTO(diaId.toString(), List.of(
                new RealizarReservaDetalleDTO("Ana Pérez", "30111222", "DNI", LocalDate.now().minusYears(30))));
    }

    // ---------- Creación de la reserva ----------

    @Test
    void iniciarUnaReservaPendienteAvisaElPlazoParaPagar() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(10).withNano(0);
        RealizarReservaDTO dto = prepararInicioDeReserva(inicio, EstadoPagoNombre.PENDIENTE);

        service.handleIniciarReserva(dto, EMAIL);

        verify(notificacionService).crearNotificacion(eq(usuario), eq(TipoNotificacionNombre.RESERVA_INICIADA), isNull(),
                anyString(), eq("Cosecha"), eq(inicio.format(FORMATO)), anyString());
        verify(notificacionService, never()).crearNotificacion(any(), eq(TipoNotificacionNombre.RESERVA_PAGADA), any(), any(), any(Object[].class));
    }

    @Test
    void iniciarUnaReservaConPagoAprobadoAvisaQueQuedoConfirmada() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(10).withNano(0);
        RealizarReservaDTO dto = prepararInicioDeReserva(inicio, EstadoPagoNombre.APROBADO);

        service.handleIniciarReserva(dto, EMAIL);

        verify(notificacionService).crearNotificacion(eq(usuario), eq(TipoNotificacionNombre.RESERVA_PAGADA), isNull(),
                anyString(), eq("Cosecha"), eq(inicio.format(FORMATO)), eq("10000.00"));
        verify(notificacionService, never()).crearNotificacion(any(), eq(TipoNotificacionNombre.RESERVA_INICIADA), any(), any(), any(Object[].class));
    }

    // ---------- Expiración y pago ----------

    @Test
    void expirarUnaReservaPorTiempoAvisaAlVisitante() {
        Reserva reserva = reservaPagada(MetodoPago.MERCADO_PAGO, LocalDateTime.now().plusDays(10));
        reserva.cambiarEstado(new EstadoReserva(EstadoReservaNombre.PENDIENTE), LocalDateTime.now());
        reserva.setFechaHoraExpiracion(LocalDateTime.now().minusMinutes(1));
        when(reservaRepository.findReservasExpiradas(any())).thenReturn(List.of(reserva));
        doAnswer(inv -> {
            service.expirarReservaYNotificar(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2));
            return null;
        }).when(self).expirarReservaYNotificar(any(), any(), any());

        service.expirarReservas();

        assertEquals(EstadoReservaNombre.EXPIRADA, reserva.getEstadoActual().getEstadoReserva().getNombre());
        verify(reservaRepository).save(reserva);
        verify(notificacionService).crearNotificacion(usuario, TipoNotificacionNombre.RESERVA_EXPIRADA, null,
                link(reserva), "Cosecha", fecha(reserva));
    }

    @Test
    void confirmarElPagoDeUnaReservaAvisaAlVisitante() {
        Reserva reserva = reservaPagada(MetodoPago.MERCADO_PAGO, LocalDateTime.now().plusDays(10));
        reserva.cambiarEstado(new EstadoReserva(EstadoReservaNombre.PENDIENTE), LocalDateTime.now());

        service.confirmarPagoReservaYNotificar(reserva, new EstadoReserva(EstadoReservaNombre.PAGADA), LocalDateTime.now());

        assertEquals(EstadoReservaNombre.PAGADA, reserva.getEstadoActual().getEstadoReserva().getNombre());
        verify(reservaRepository).save(reserva);
        verify(notificacionService).crearNotificacion(usuario, TipoNotificacionNombre.RESERVA_PAGADA, null,
                link(reserva), "Cosecha", fecha(reserva), "10000.00");
    }

    // ---------- Cancelación ----------

    @Test
    void cancelarSinReembolsoAvisaQueNoCorrespondeReembolso() {
        Reserva reserva = reservaPagada(MetodoPago.MERCADO_PAGO, LocalDateTime.now().plusDays(2));

        assertNull(service.cancelarReservaYCrearReembolso(reserva.getId().toString(), EMAIL));

        assertEquals(EstadoReservaNombre.CANCELADA_SIN_REEMBOLSO, reserva.getEstadoActual().getEstadoReserva().getNombre());
        verify(notificacionService).crearNotificacion(usuario, TipoNotificacionNombre.RESERVA_CANCELADA_SIN_REEMBOLSO, null,
                link(reserva), "Cosecha", fecha(reserva), 5);
    }

    @Test
    void cancelarConReembolsoPorMpAvisaQueElReembolsoEstaEnProceso() {
        Reserva reserva = reservaPagada(MetodoPago.MERCADO_PAGO, LocalDateTime.now().plusDays(30));

        Reembolso reembolso = service.cancelarReservaYCrearReembolso(reserva.getId().toString(), EMAIL);

        assertEquals(EstadoReembolsoNombre.EN_PROCESO, reembolso.getEstadoActual().getEstadoReembolso().getNombre());
        assertEquals(EstadoReservaNombre.CANCELADA_REEMBOLSO_PENDIENTE, reserva.getEstadoActual().getEstadoReserva().getNombre());
        verify(notificacionService).crearNotificacion(usuario, TipoNotificacionNombre.RESERVA_CANCELADA_REEMBOLSO_EN_PROCESO, null,
                link(reserva), "Cosecha", fecha(reserva), "10000.00");
        verifyNoMoreInteractions(notificacionService);
    }

    @Test
    void cancelarConReembolsoManualAvisaQueSeReembolso() {
        Reserva reserva = reservaPagada(MetodoPago.MANUAL, LocalDateTime.now().plusDays(30));

        Reembolso reembolso = service.cancelarReservaYCrearReembolso(reserva.getId().toString(), EMAIL);

        assertEquals(EstadoReembolsoNombre.REEMBOLSADO_PRODUCTOR, reembolso.getEstadoActual().getEstadoReembolso().getNombre());
        assertEquals(EstadoReservaNombre.CANCELADA_CON_REEMBOLSO, reserva.getEstadoActual().getEstadoReserva().getNombre());
        verify(notificacionService).crearNotificacion(usuario, TipoNotificacionNombre.RESERVA_CANCELADA_CON_REEMBOLSO, null,
                link(reserva), "Cosecha", fecha(reserva), "10000.00");
        verifyNoMoreInteractions(notificacionService);
    }

    // ---------- Reembolsos ----------

    @Test
    void siLaPasarelaRechazaElPedidoDeReembolsoAvisaQueSeGestionaraManualmente() {
        Reserva reserva = reservaPagada(MetodoPago.MERCADO_PAGO, LocalDateTime.now().plusDays(30));
        Reembolso reembolso = reembolso(reserva);

        service.registrarPedidoReembolso(reembolso.getId(), ResultadoReembolsoDTO.rechazado(), true);

        assertEquals(EstadoReembolsoNombre.PEDIDO, reembolso.getEstadoActual().getEstadoReembolso().getNombre());
        verify(notificacionService).crearNotificacion(usuario, TipoNotificacionNombre.REEMBOLSO_NO_AUTOMATICO, null,
                link(reserva), "Cosecha", fecha(reserva), "10000.00");
    }

    @Test
    void siLaPasarelaRechazaElReintentoDelProductorNoVuelveAAvisarAlVisitante() {
        Reserva reserva = reservaPagada(MetodoPago.MERCADO_PAGO, LocalDateTime.now().plusDays(30));
        Reembolso reembolso = reembolso(reserva);

        service.registrarPedidoReembolso(reembolso.getId(), ResultadoReembolsoDTO.rechazado(), false);

        assertEquals(EstadoReembolsoNombre.PEDIDO, reembolso.getEstadoActual().getEstadoReembolso().getNombre());
        verifyNoInteractions(notificacionService);
    }

    @Test
    void siLaPasarelaAceptaElPedidoDeReembolsoNoAvisaHastaQueSeConfirme() {
        Reserva reserva = reservaPagada(MetodoPago.MERCADO_PAGO, LocalDateTime.now().plusDays(30));
        Reembolso reembolso = reembolso(reserva);

        service.registrarPedidoReembolso(reembolso.getId(), new ResultadoReembolsoDTO(true, "555"), true);

        assertEquals("555", reembolso.getIdReembolsoExterno());
        verifyNoInteractions(notificacionService);
    }

    @Test
    void guardarUnReembolsoAvisaAlVisitanteConElMonto() {
        Reserva reserva = reservaPagada(MetodoPago.MERCADO_PAGO, LocalDateTime.now().plusDays(30));
        Reembolso reembolso = reembolso(reserva);

        service.guardarReembolsoYNotificar(reembolso, TipoNotificacionNombre.REEMBOLSO_REALIZADO);

        verify(reembolsoRepository).save(reembolso);
        verify(notificacionService).crearNotificacion(usuario, TipoNotificacionNombre.REEMBOLSO_REALIZADO, null,
                link(reserva), "Cosecha", fecha(reserva), "10000.00");
    }
}
