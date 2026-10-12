package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.entities.Parametros;
import com.mza_agrotours.backend.entities.pago.EstadoReembolso;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.entities.pago.ReembolsoEstado;
import com.mza_agrotours.backend.enums.EstadoReembolsoNombre;
import com.mza_agrotours.backend.mappers.reserva.ReservaMapper;
import com.mza_agrotours.backend.repositories.*;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoPagoRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoReembolsoRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoSistemaPagoRepository;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import com.mza_agrotours.backend.services.pago.EstrategiaPagoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
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
class ReservaServiceReembolsoImpagoTest {

    private ReembolsoRepository reembolsoRepository;
    private ReservaService self;
    private ReservaService service;

    private final Parametros parametros = new Parametros();

    @BeforeEach
    void setUp() {
        reembolsoRepository = mock(ReembolsoRepository.class);
        self = mock(ReservaService.class);

        ParametrosService parametrosService = mock(ParametrosService.class);
        when(parametrosService.getInstance()).thenReturn(parametros);

        EstadoReembolsoRepository estadoReembolsoRepository = mock(EstadoReembolsoRepository.class);
        when(estadoReembolsoRepository.findByNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoReembolso(LocalDateTime.now(), null, inv.<EstadoReembolsoNombre>getArgument(0))));

        service = new ReservaService(mock(ReservaRepository.class), mock(ReservaMapper.class), mock(ActividadRepository.class),
                parametrosService, mock(UsuarioRepository.class), mock(VisitanteRepository.class),
                mock(TipoIdentificacionRepository.class), mock(EstrategiaPagoFactory.class), self,
                mock(EstadoPagoRepository.class), mock(NotificacionService.class), estadoReembolsoRepository, reembolsoRepository,
                mock(ReembolsoSistemaPagoRepository.class), mock(AdministradorSistemasRepository.class));
    }

    // ---------- Datos ----------

    private Reembolso reembolso(EstadoReembolsoNombre estado) {
        Reembolso reembolso = new Reembolso();
        reembolso.setId(UUID.randomUUID());
        reembolso.setFechaHoraPedido(LocalDateTime.now().minusDays(10));
        reembolso.cambiarEstado(new EstadoReembolso(LocalDateTime.now(), null, estado), LocalDateTime.now().minusDays(10));
        when(reembolsoRepository.findById(reembolso.getId())).thenReturn(Optional.of(reembolso));
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
    void unReembolsoQueYaNoEstaPedidoNoCambia() {
        Reembolso reembolso = reembolso(EstadoReembolsoNombre.REEMBOLSADO_PRODUCTOR);

        service.marcarReembolsoImpago(reembolso.getId(), LocalDateTime.now());

        assertEquals(EstadoReembolsoNombre.REEMBOLSADO_PRODUCTOR, reembolso.getEstadoActual().getEstadoReembolso().getNombre());
        assertEquals(1, reembolso.getEstados().size());
        verify(reembolsoRepository, never()).save(any());
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
