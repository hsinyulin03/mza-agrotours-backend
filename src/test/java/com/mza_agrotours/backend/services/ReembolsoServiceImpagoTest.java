package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.entities.Parametros;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.pago.EstadoReembolso;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.entities.pago.ReembolsoEstado;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.EstadoReembolsoNombre;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Paso a "Impago" de los reembolsos "Pedido" que el productor no pagó a tiempo.
 */
class ReembolsoServiceImpagoTest {

    private ReembolsoRepository reembolsoRepository;
    private NotificacionService notificacionService;
    private ReembolsoService self;
    private ReembolsoService service;

    private final Parametros parametros = new Parametros();

    @BeforeEach
    void setUp() {
        reembolsoRepository = mock(ReembolsoRepository.class);
        notificacionService = mock(NotificacionService.class);
        self = mock(ReembolsoService.class);

        ParametrosService parametrosService = mock(ParametrosService.class);
        when(parametrosService.getInstance()).thenReturn(parametros);

        EstadoReembolsoRepository estadoReembolsoRepository = mock(EstadoReembolsoRepository.class);
        when(estadoReembolsoRepository.findByNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoReembolso(LocalDateTime.now(), null, inv.<EstadoReembolsoNombre>getArgument(0))));

        service = new ReembolsoService(reembolsoRepository, mock(ReembolsoSistemaPagoRepository.class),
                mock(EstadoPagoRepository.class), estadoReembolsoRepository, mock(AdministradorSistemasRepository.class),
                mock(ReservaRepository.class), mock(ReembolsoMapper.class), mock(EstrategiaPagoFactory.class),
                parametrosService, new ReservaNotificador(notificacionService), self);
    }

    // ---------- Datos ----------

    private final Usuario usuarioVisitante = mock(Usuario.class);
    private final Establecimiento establecimiento = establecimiento();
    private final LocalDateTime inicioActividad = LocalDateTime.of(2026, 11, 20, 9, 30);

    private static Establecimiento establecimiento() {
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setId(UUID.randomUUID());
        establecimiento.setNombre("Finca Los Álamos");
        return establecimiento;
    }

    /** Reserva de la actividad "Cosecha" del establecimiento, del visitante de prueba. */
    private Reserva reserva() {
        Actividad actividad = mock(Actividad.class);
        when(actividad.getNombre()).thenReturn("Cosecha");
        when(actividad.getEstablecimiento()).thenReturn(establecimiento);
        ActividadDia dia = mock(ActividadDia.class);
        when(dia.getFechaHoraInicio()).thenReturn(inicioActividad);
        Visitante visitante = mock(Visitante.class);
        when(visitante.getUsuario()).thenReturn(usuarioVisitante);

        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID());
        reserva.setActividad(actividad);
        reserva.setActividadDia(dia);
        reserva.setVisitante(visitante);
        return reserva;
    }

    private Reembolso reembolso(EstadoReembolsoNombre estado) {
        Reembolso reembolso = new Reembolso();
        reembolso.setId(UUID.randomUUID());
        reembolso.setReserva(reserva());
        reembolso.setMontoReembolso(new BigDecimal("10000.00"));
        reembolso.setFechaHoraPedido(LocalDateTime.now().minusDays(10));
        reembolso.cambiarEstado(new EstadoReembolso(LocalDateTime.now(), null, estado), LocalDateTime.now().minusDays(10));
        when(reembolsoRepository.findByIdForUpdate(reembolso.getId())).thenReturn(Optional.of(reembolso));
        return reembolso;
    }

    // ---------- marcarReembolsoImpago ----------

    @Test
    void unReembolsoPedidoPasaAImpago() {
        Reembolso reembolso = reembolso(EstadoReembolsoNombre.PEDIDO);
        ReembolsoEstado estadoPedido = reembolso.getEstadoActual();
        LocalDateTime ahora = LocalDateTime.now();

        service.marcarReembolsoImpago(reembolso.getId(), ahora);

        assertEquals(EstadoReembolsoNombre.IMPAGO, reembolso.getEstadoActual().getEstadoReembolso().getNombre());
        assertEquals(ahora, reembolso.getEstadoActual().getFechaHoraInicio());
        assertEquals(ahora, estadoPedido.getFechaHoraFin());
        assertEquals(2, reembolso.getEstados().size());
        verify(reembolsoRepository).save(reembolso);
    }

    @Test
    void pasarAImpagoAvisaAlVisitanteALosProductoresYALosAdministradores() {
        Reembolso reembolso = reembolso(EstadoReembolsoNombre.PEDIDO);
        Reserva reserva = reembolso.getReserva();
        String fecha = inicioActividad.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        service.marcarReembolsoImpago(reembolso.getId(), LocalDateTime.now());

        verify(notificacionService).crearNotificacion(usuarioVisitante, TipoNotificacionNombre.REEMBOLSO_A_CARGO_AGROTOURS, null,
                "/mis-reservas/" + reserva.getId(), "Cosecha", fecha, "10000.00");
        verify(notificacionService).notificarProductores(establecimiento, PermisoCodigo.GESTIONAR_REEMBOLSOS_PENDIENTES,
                TipoNotificacionNombre.REEMBOLSO_IMPAGO_PRODUCTOR, "/panel/" + establecimiento.getId() + "/reservas",
                "Cosecha", fecha, "10000.00");
        verify(notificacionService).notificarAdministradores(PermisoCodigo.LEER_REEMBOLSOS,
                TipoNotificacionNombre.REEMBOLSO_IMPAGO_ADMIN, "/admin/impagas/" + reembolso.getId(),
                "Finca Los Álamos", "10000.00", "Cosecha", fecha);
        verifyNoMoreInteractions(notificacionService);
    }

    @Test
    void unReembolsoQueYaNoEstaPedidoNoCambia() {
        Reembolso reembolso = reembolso(EstadoReembolsoNombre.REEMBOLSADO_PRODUCTOR);

        service.marcarReembolsoImpago(reembolso.getId(), LocalDateTime.now());

        assertEquals(EstadoReembolsoNombre.REEMBOLSADO_PRODUCTOR, reembolso.getEstadoActual().getEstadoReembolso().getNombre());
        assertEquals(1, reembolso.getEstados().size());
        verify(reembolsoRepository, never()).save(any());
        verifyNoInteractions(notificacionService);
    }

    // ---------- marcarReembolsosImpagos ----------

    @Test
    void sinElParametroCargadoNoSeBuscanReembolsos() {
        parametros.setDiasImpagoReserva(null);

        service.marcarReembolsosImpagos();

        verify(reembolsoRepository, never()).findIdsPedidosVencidos(any());
        verifyNoInteractions(self);
    }

    @Test
    void seBuscanLosPedidosAnterioresAlPlazoYSeMarcaCadaUno() {
        parametros.setDiasImpagoReserva(7);
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(reembolsoRepository.findIdsPedidosVencidos(any())).thenReturn(List.of(id1, id2));

        LocalDateTime antes = LocalDateTime.now();
        service.marcarReembolsosImpagos();
        LocalDateTime despues = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> limite = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(reembolsoRepository).findIdsPedidosVencidos(limite.capture());
        assertFalse(limite.getValue().isBefore(antes.minusDays(7)));
        assertFalse(limite.getValue().isAfter(despues.minusDays(7)));

        verify(self).marcarReembolsoImpago(eq(id1), any());
        verify(self).marcarReembolsoImpago(eq(id2), any());
    }

    @Test
    void unFalloNoInterrumpeElRestoDeLosReembolsos() {
        parametros.setDiasImpagoReserva(7);
        UUID idFallido = UUID.randomUUID();
        UUID idOk = UUID.randomUUID();
        when(reembolsoRepository.findIdsPedidosVencidos(any())).thenReturn(List.of(idFallido, idOk));
        doThrow(new RuntimeException("falla")).when(self).marcarReembolsoImpago(eq(idFallido), any());

        assertDoesNotThrow(() -> service.marcarReembolsosImpagos());

        verify(self).marcarReembolsoImpago(eq(idOk), any());
    }
}
