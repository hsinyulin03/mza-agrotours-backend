package com.mza_agrotours.backend.services.outbox;

import com.mza_agrotours.backend.config.outbox.OutboxStrategy;
import com.mza_agrotours.backend.config.outbox.OutboxStrategyFactory;
import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.enums.outbox.EstadoOutbox;
import com.mza_agrotours.backend.enums.outbox.TipoOperacion;
import com.mza_agrotours.backend.repositories.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Ciclo de vida de una fila del outbox sin Spring ni base: que estado y que proximo intento
 * le quedan despues de cada resolucion, y que filas toma el poller.
 */
class OutboxServiceTest {

    private OutboxRepository outboxRepository;
    private OutboxStrategy strategy;
    private OutboxService outboxService;

    @BeforeEach
    void setUp() {
        this.outboxRepository = mock(OutboxRepository.class);
        this.strategy = mock(OutboxStrategy.class);

        OutboxStrategyFactory factory = mock(OutboxStrategyFactory.class);
        when(factory.getStrategy(any())).thenReturn(strategy);
        when(outboxRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        this.outboxService = new OutboxService(outboxRepository, factory);
    }

    @Test
    void cuandoSeCreaUnaFila_entoncesQuedaPendienteConPeriodoDeGracia() {
        Outbox outbox = outboxService.crearOutboxPendiente("id-entidad", TipoOperacion.ELIMINAR_USUARIO);

        assertThat(outbox.getEstado()).isEqualTo(EstadoOutbox.PENDIENTE);
        assertThat(outbox.getReintentos()).isZero();
        assertThat(outbox.getOperacion()).isEqualTo(TipoOperacion.ELIMINAR_USUARIO);
        assertThat(Duration.between(outbox.getFechaHoraAlta(), outbox.getFechaHoraProximoIntento()))
                .as("el poller no la toma antes de que el evento haga el primer intento")
                .isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void dadoQueLaEstrategiaResuelve_cuandoSeResuelve_entoncesQuedaExitosa() throws Exception {
        Outbox outbox = pendiente(0, LocalDateTime.now());

        outboxService.resolverOutbox(outbox);

        verify(strategy).resolver(outbox);
        verify(outboxRepository).save(outbox);
        assertThat(outbox.getEstado()).isEqualTo(EstadoOutbox.EXITOSO);
        assertThat(outbox.getReintentos()).isZero();
    }

    @Test
    void dadoQueLaEstrategiaFalla_cuandoSeResuelve_entoncesSiguePendienteConUnReintento() throws Exception {
        Outbox outbox = pendiente(0, LocalDateTime.now());
        doThrow(new RuntimeException("Firebase caido")).when(strategy).resolver(outbox);

        outboxService.resolverOutbox(outbox);

        verify(outboxRepository).save(outbox);
        assertThat(outbox.getEstado()).isEqualTo(EstadoOutbox.PENDIENTE);
        assertThat(outbox.getReintentos()).isEqualTo(1);
    }

    @ParameterizedTest(name = "reintentos previos {0} -> espera entre {1} y {2} segundos")
    @CsvSource({
            "0, 3, 7",
            "1, 5, 9",
            "2, 9, 13",
            "3, 17, 21"
    })
    void cuandoFalla_entoncesElProximoIntentoCreceExponencialmenteConJitter(int reintentosPrevios, int minSeg, int maxSeg) {
        Outbox outbox = pendiente(reintentosPrevios, LocalDateTime.now());

        LocalDateTime antes = LocalDateTime.now();
        outboxService.handleIntentoFallido(outbox);
        LocalDateTime despues = LocalDateTime.now();

        assertThat(outbox.getReintentos()).isEqualTo(reintentosPrevios + 1);
        assertThat(outbox.getFechaHoraProximoIntento())
                .isAfterOrEqualTo(antes.plusSeconds(minSeg))
                .isBeforeOrEqualTo(despues.plusSeconds(maxSeg));
    }

    @Test
    void dadoQueAgotoLosReintentos_cuandoFallaOtraVez_entoncesQuedaFallida() {
        Outbox outbox = pendiente(4, LocalDateTime.now());

        outboxService.handleIntentoFallido(outbox);

        verify(outboxRepository).save(outbox);
        assertThat(outbox.getEstado()).isEqualTo(EstadoOutbox.FALLIDO);
        assertThat(outbox.getReintentos()).isEqualTo(4);
        assertThat(outbox.getFechaHoraProximoIntento())
                .as("la columna es NOT NULL")
                .isNotNull();
    }

    @Test
    void cuandoCorreElPoller_entoncesSoloResuelveLasFilasVencidas() throws Exception {
        Outbox vencida = pendiente(1, LocalDateTime.now().minusSeconds(1));
        Outbox futura = pendiente(1, LocalDateTime.now().plusMinutes(1));
        when(outboxRepository.findByEstado(EstadoOutbox.PENDIENTE)).thenReturn(List.of(vencida, futura));

        outboxService.resolverPendientes();

        verify(strategy).resolver(vencida);
        verify(strategy, never()).resolver(futura);
        assertThat(futura.getEstado()).isEqualTo(EstadoOutbox.PENDIENTE);
    }

    @Test
    void dadoQueUnaFilaFalla_cuandoCorreElPoller_entoncesLasDemasSeResuelvenIgual() throws Exception {
        Outbox rota = pendiente(0, LocalDateTime.now().minusSeconds(1));
        Outbox sana = pendiente(0, LocalDateTime.now().minusSeconds(1));
        when(outboxRepository.findByEstado(EstadoOutbox.PENDIENTE)).thenReturn(List.of(rota, sana));
        doThrow(new IllegalStateException("payload invalido")).when(strategy).resolver(rota);

        outboxService.resolverPendientes();

        assertThat(rota.getEstado()).isEqualTo(EstadoOutbox.PENDIENTE);
        assertThat(rota.getReintentos()).isEqualTo(1);
        assertThat(sana.getEstado()).isEqualTo(EstadoOutbox.EXITOSO);
    }

    private static Outbox pendiente(int reintentos, LocalDateTime proximoIntento) {
        Outbox outbox = new Outbox();
        outbox.setEntidadId("id-entidad");
        outbox.setOperacion(TipoOperacion.ELIMINAR_USUARIO);
        outbox.setEstado(EstadoOutbox.PENDIENTE);
        outbox.setReintentos(reintentos);
        outbox.setFechaHoraAlta(LocalDateTime.now());
        outbox.setFechaHoraProximoIntento(proximoIntento);
        return outbox;
    }
}
