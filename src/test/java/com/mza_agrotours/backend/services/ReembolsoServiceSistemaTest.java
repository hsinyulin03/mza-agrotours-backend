package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.pago.RegistrarPagoReembolsoSistemaDTO;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.EstadoReembolso;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.entities.pago.ReembolsoSistemaPago;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.*;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.pago.ReembolsoNoImpagoException;
import com.mza_agrotours.backend.mappers.ReembolsoMapper;
import com.mza_agrotours.backend.repositories.*;
import com.mza_agrotours.backend.repositories.pago.EstadoPagoRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoReembolsoRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoSistemaPagoRepository;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import com.mza_agrotours.backend.services.notificaciones.ReservaNotificador;
import com.mza_agrotours.backend.services.pago.EstrategiaPagoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Registro por parte de un administrador del pago de un reembolso que el productor no pagó (US-RESE-10).
 */
class ReembolsoServiceSistemaTest {

    private static final String EMAIL_ADMIN = "admin@test.com";

    private ReservaRepository reservaRepository;
    private ReembolsoRepository reembolsoRepository;
    private ReembolsoSistemaPagoRepository reembolsoSistemaPagoRepository;
    private AdministradorSistemasRepository administradorSistemasRepository;
    private ReembolsoMapper reembolsoMapper;
    private NotificacionService notificacionService;
    private ReembolsoService service;

    private final AdministradorSistemas administrador = new AdministradorSistemas();
    private final Usuario usuarioVisitante = mock(Usuario.class);
    private final Establecimiento establecimiento = new Establecimiento();

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        reembolsoRepository = mock(ReembolsoRepository.class);
        reembolsoSistemaPagoRepository = mock(ReembolsoSistemaPagoRepository.class);
        administradorSistemasRepository = mock(AdministradorSistemasRepository.class);
        reembolsoMapper = mock(ReembolsoMapper.class);
        notificacionService = mock(NotificacionService.class);
        establecimiento.setId(UUID.randomUUID());

        when(administradorSistemasRepository.findByEmailActivo(EMAIL_ADMIN)).thenReturn(Optional.of(administrador));

        EstadoPagoRepository estadoPagoRepository = mock(EstadoPagoRepository.class);
        EstadoReembolsoRepository estadoReembolsoRepository = mock(EstadoReembolsoRepository.class);
        when(reservaRepository.findEstadoReservaByEstadoReservaNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoReserva(inv.<EstadoReservaNombre>getArgument(0))));
        when(estadoPagoRepository.findByNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoPago(inv.<EstadoPagoNombre>getArgument(0), LocalDateTime.now(), null)));
        when(estadoReembolsoRepository.findByNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoReembolso(LocalDateTime.now(), null, inv.<EstadoReembolsoNombre>getArgument(0))));

        service = new ReembolsoService(reembolsoRepository, reembolsoSistemaPagoRepository, estadoPagoRepository,
                estadoReembolsoRepository, administradorSistemasRepository, reservaRepository, reembolsoMapper,
                mock(EstrategiaPagoFactory.class), mock(ParametrosService.class),
                new ReservaNotificador(notificacionService), mock(ReembolsoService.class));
    }

    // ---------- Datos ----------

    /** Reserva en el estado indicado, con su pago original aprobado. */
    private Reserva reserva(EstadoReservaNombre estado) {
        Visitante visitante = mock(Visitante.class);
        when(visitante.getUsuario()).thenReturn(usuarioVisitante);
        Actividad actividad = mock(Actividad.class);
        when(actividad.getNombre()).thenReturn("Cosecha");
        when(actividad.getEstablecimiento()).thenReturn(establecimiento);
        ActividadDia dia = mock(ActividadDia.class);
        when(dia.getFechaHoraInicio()).thenReturn(LocalDateTime.now().plusDays(10));

        Pago pago = new Pago();
        pago.setMetodoPago(MetodoPago.MERCADO_PAGO);
        pago.setMontoTotal(new BigDecimal("10000.00"));
        pago.cambiarEstado(new EstadoPago(EstadoPagoNombre.APROBADO, LocalDateTime.now(), null), LocalDateTime.now());

        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID());
        reserva.setVisitante(visitante);
        reserva.setActividad(actividad);
        reserva.setActividadDia(dia);
        reserva.setPago(pago);
        reserva.cambiarEstado(new EstadoReserva(estado), LocalDateTime.now());
        return reserva;
    }

    private Reembolso reembolso(Reserva reserva, EstadoReembolsoNombre estado) {
        Reembolso reembolso = new Reembolso();
        reembolso.setId(UUID.randomUUID());
        reembolso.setReserva(reserva);
        reembolso.setMontoReembolso(new BigDecimal("10000.00"));
        reembolso.cambiarEstado(new EstadoReembolso(LocalDateTime.now(), null, estado), LocalDateTime.now());
        when(reembolsoRepository.findByIdAndEstadoActualIn(eq(reembolso.getId()), any())).thenReturn(Optional.of(reembolso));
        return reembolso;
    }

    private static RegistrarPagoReembolsoSistemaDTO dto(LocalDateTime fechaHoraPago) {
        return new RegistrarPagoReembolsoSistemaDTO(fechaHoraPago, "Transferencia bancaria", "TRX-123");
    }

    // ---------- Casos ----------

    @Test
    void registrarElPagoDeUnReembolsoImpagoLoDejaReembolsadoPorSistema() {
        Reserva reserva = reserva(EstadoReservaNombre.CANCELADA_REEMBOLSO_PENDIENTE);
        Reembolso reembolso = reembolso(reserva, EstadoReembolsoNombre.IMPAGO);
        LocalDateTime fechaHoraPago = LocalDateTime.now().minusHours(3).withNano(0);

        service.registrarPagoReembolsoSistema(reembolso.getId(), dto(fechaHoraPago), EMAIL_ADMIN);

        ArgumentCaptor<ReembolsoSistemaPago> captor = ArgumentCaptor.forClass(ReembolsoSistemaPago.class);
        verify(reembolsoSistemaPagoRepository).save(captor.capture());
        ReembolsoSistemaPago vinculo = captor.getValue();
        assertSame(reembolso, vinculo.getReembolso());
        assertSame(administrador, vinculo.getCargadoPorAdmin());
        assertEquals("Transferencia bancaria", vinculo.getMetodoPagoReal());
        assertNotNull(vinculo.getFechaHoraCarga());

        Pago pago = vinculo.getPago();
        assertEquals(MetodoPago.MANUAL, pago.getMetodoPago());
        assertEquals(EstadoPagoNombre.APROBADO, pago.getEstadoActual().getEstadoPago().getNombre());
        assertEquals(new BigDecimal("10000.00"), pago.getMontoTotal());
        assertEquals("TRX-123", pago.getIdTransaccionExterna());
        assertEquals(fechaHoraPago, pago.getFechaHoraPago());

        assertEquals(EstadoReembolsoNombre.REEMBOLSADO_SISTEMA, reembolso.getEstadoActual().getEstadoReembolso().getNombre());
        assertEquals(fechaHoraPago, reembolso.getFechaHoraReembolso());
        assertEquals(EstadoPagoNombre.REEMBOLSADO, reserva.getPago().getEstadoActual().getEstadoPago().getNombre());
        assertEquals(EstadoReservaNombre.CANCELADA_CON_REEMBOLSO, reserva.getEstadoActual().getEstadoReserva().getNombre());

        verify(notificacionService).crearNotificacion(eq(usuarioVisitante), eq(TipoNotificacionNombre.REEMBOLSO_REALIZADO),
                any(), any(), eq("Cosecha"), any(), eq("10000.00"));
        verify(notificacionService).notificarProductores(eq(establecimiento), eq(PermisoCodigo.GESTIONAR_REEMBOLSOS_PENDIENTES),
                eq(TipoNotificacionNombre.REEMBOLSO_SISTEMA_PRODUCTOR), eq("/panel/" + establecimiento.getId() + "/reservas"),
                eq("Cosecha"), any(), eq("10000.00"));
        verify(reembolsoMapper).reembolsoToDetalleReembolsoAdminDTO(reembolso, vinculo);
    }

    @Test
    void laReservaDeUnPagoNoConciliadoNoCambiaDeEstado() {
        Reserva reserva = reserva(EstadoReservaNombre.REEMBOLSO_NO_CONCILIACION);
        Reembolso reembolso = reembolso(reserva, EstadoReembolsoNombre.IMPAGO);

        service.registrarPagoReembolsoSistema(reembolso.getId(), dto(LocalDateTime.now().minusHours(1)), EMAIL_ADMIN);

        assertEquals(EstadoReservaNombre.REEMBOLSO_NO_CONCILIACION, reserva.getEstadoActual().getEstadoReserva().getNombre());
        assertEquals(EstadoPagoNombre.REEMBOLSADO, reserva.getPago().getEstadoActual().getEstadoPago().getNombre());
    }

    @Test
    void unReembolsoYaPagadoPorElSistemaNoSePuedeVolverAPagar() {
        Reserva reserva = reserva(EstadoReservaNombre.CANCELADA_CON_REEMBOLSO);
        Reembolso reembolso = reembolso(reserva, EstadoReembolsoNombre.REEMBOLSADO_SISTEMA);

        assertThrows(ReembolsoNoImpagoException.class, () ->
                service.registrarPagoReembolsoSistema(reembolso.getId(), dto(LocalDateTime.now().minusHours(1)), EMAIL_ADMIN));

        verify(reembolsoSistemaPagoRepository, never()).save(any());
        verifyNoInteractions(notificacionService);
    }

    @Test
    void unAdministradorInexistenteNoPuedeRegistrarElPago() {
        Reserva reserva = reserva(EstadoReservaNombre.CANCELADA_REEMBOLSO_PENDIENTE);
        Reembolso reembolso = reembolso(reserva, EstadoReembolsoNombre.IMPAGO);

        assertThrows(AppException.class, () ->
                service.registrarPagoReembolsoSistema(reembolso.getId(), dto(LocalDateTime.now().minusHours(1)), "otro@test.com"));

        assertEquals(EstadoReembolsoNombre.IMPAGO, reembolso.getEstadoActual().getEstadoReembolso().getNombre());
        verify(reembolsoSistemaPagoRepository, never()).save(any());
    }
}
