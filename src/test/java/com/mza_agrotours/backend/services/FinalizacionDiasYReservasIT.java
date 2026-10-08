package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.schedules.ActividadDiaFinalizadaScheduler;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureActividad;
import com.mza_agrotours.backend.support.FixtureCatalogo;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import com.mza_agrotours.backend.support.FixtureUsuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cierre automático de días de actividad y sus reservas: los días "Activa" o "Reprogramada" que ya terminaron
 * pasan a "Finalizada", y las reservas "Pagada" de un día "Finalizada" pasan a "Finalizada".
 * Sin @Transactional a proposito: cada reserva se finaliza en su propia transacción (REQUIRES_NEW),
 * que solo ve lo que el test haya commiteado. Por eso los datos se arman y se leen con TransactionTemplate.
 */
class FinalizacionDiasYReservasIT extends AbstractIntegrationTest {

    @Autowired private ActividadDiaFinalizadaScheduler scheduler;
    @Autowired private ReservaService reservaService;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    @Autowired private FixtureEstablecimiento establecimientos;
    @Autowired private FixtureActividad actividades;
    @Autowired private FixtureUsuario usuarios;
    @Autowired private FixtureCatalogo catalogo;

    @PersistenceContext private EntityManager entityManager;

    private UUID idActividad;

    @BeforeEach
    void setUp() {
        this.idActividad = transactionTemplate.execute(status ->
                actividades.actividadPublicadaEn(establecimientos.establecimientoActivo()).getId());
    }

    // DÍAS

    @Test
    void dadoUnDiaActivoQueYaTermino_cuandoCorreElScheduler_entoncesElDiaQuedaFinalizado() {
        UUID dia = crearDia(LocalDateTime.now().minusDays(1), EstadoActividadDiaNombre.ACTIVA);

        scheduler.checkDiasTerminados();

        transactionTemplate.executeWithoutResult(status -> {
            ActividadDia actividadDia = entityManager.find(ActividadDia.class, dia);
            assertThat(actividadDia.getEstadoActual().getEstado().getNombre())
                    .isEqualTo(EstadoActividadDiaNombre.FINALIZADA);
            assertThat(actividadDia.getEstadoActual().getMotivo()).isEqualTo("Finalización automática del día");
            assertThat(actividadDia.getEstados()).hasSize(2);
            // El estado anterior quedo cerrado
            assertThat(actividadDia.getEstados().get(0).getFechaHoraFin()).isNotNull();
        });
    }

    @Test
    void dadoUnDiaReprogramadoQueYaTermino_cuandoCorreElScheduler_entoncesElDiaQuedaFinalizado() {
        UUID dia = crearDia(LocalDateTime.now().minusDays(1), EstadoActividadDiaNombre.REPROGRAMADA);

        scheduler.checkDiasTerminados();

        assertThat(estadoDelDia(dia)).isEqualTo(EstadoActividadDiaNombre.FINALIZADA);
    }

    @Test
    void dadoUnDiaEnCurso_cuandoCorreElScheduler_entoncesElDiaSigueActivo() {
        LocalDateTime ahora = LocalDateTime.now();
        UUID dia = crearDia(ahora.minusHours(1), ahora.plusHours(2), EstadoActividadDiaNombre.ACTIVA);

        scheduler.checkDiasTerminados();

        assertThat(estadoDelDia(dia)).isEqualTo(EstadoActividadDiaNombre.ACTIVA);
    }

    @Test
    void dadoUnDiaFuturo_cuandoCorreElScheduler_entoncesNiElDiaNiSusReservasPagadasCambian() {
        UUID dia = crearDia(LocalDateTime.now().plusDays(3), EstadoActividadDiaNombre.ACTIVA);
        UUID pagada = crearReserva(dia, EstadoReservaNombre.PAGADA);

        scheduler.checkDiasTerminados();

        assertThat(estadoDelDia(dia)).isEqualTo(EstadoActividadDiaNombre.ACTIVA);
        assertThat(estadoDeReserva(pagada)).isEqualTo(EstadoReservaNombre.PAGADA);
    }

    @Test
    void dadoUnDiaCanceladoQueYaPaso_cuandoCorreElScheduler_entoncesNiElDiaNiSusReservasPagadasCambian() {
        UUID dia = crearDia(LocalDateTime.now().minusDays(5), EstadoActividadDiaNombre.CANCELADA);
        UUID pagada = crearReserva(dia, EstadoReservaNombre.PAGADA);

        scheduler.checkDiasTerminados();

        assertThat(estadoDelDia(dia)).isEqualTo(EstadoActividadDiaNombre.CANCELADA);
        assertThat(estadoDeReserva(pagada)).isEqualTo(EstadoReservaNombre.PAGADA);
    }

    @Test
    void dadoUnDiaDadoDeBajaQueYaPaso_cuandoCorreElScheduler_entoncesElDiaNoSeToca() {
        UUID dia = crearDia(LocalDateTime.now().minusDays(1), EstadoActividadDiaNombre.ACTIVA);
        transactionTemplate.executeWithoutResult(status ->
                entityManager.find(ActividadDia.class, dia).setFechaHoraBaja(LocalDateTime.now()));

        scheduler.checkDiasTerminados();

        assertThat(estadoDelDia(dia)).isEqualTo(EstadoActividadDiaNombre.ACTIVA);
    }

    // RESERVAS

    @Test
    void dadoUnDiaQueYaTermino_cuandoCorreElScheduler_entoncesFinalizaElDiaYSusReservasPagadas() {
        UUID dia = crearDia(LocalDateTime.now().minusDays(1), EstadoActividadDiaNombre.ACTIVA);
        UUID pagada = crearReserva(dia, EstadoReservaNombre.PAGADA);

        scheduler.checkDiasTerminados();

        assertThat(estadoDelDia(dia)).isEqualTo(EstadoActividadDiaNombre.FINALIZADA);
        assertThat(estadoDeReserva(pagada)).isEqualTo(EstadoReservaNombre.FINALIZADA);
        transactionTemplate.executeWithoutResult(status -> {
            Reserva reserva = reservaRepository.findById(pagada).orElseThrow();
            assertThat(reserva.getFechaHoraFin()).isNotNull();   // es un estado final
            assertThat(reserva.getEstados()).hasSize(2);
            assertThat(reserva.getEstados().get(0).getFechaHoraFin()).isNotNull();
        });
    }

    /**
     * Cubre las pagadas tarde (el pago se aprobó con el día ya finalizado) y los días finalizados
     * antes de que existiera el cierre de reservas: la búsqueda no depende de la corrida del día.
     */
    @Test
    void dadoUnDiaYaFinalizado_cuandoSeFinalizanReservas_entoncesFinalizaLasPagadasQueQuedaron() {
        UUID dia = crearDia(LocalDateTime.now().minusDays(5), EstadoActividadDiaNombre.FINALIZADA);
        UUID pagada = crearReserva(dia, EstadoReservaNombre.PAGADA);

        reservaService.finalizarReservas();

        assertThat(estadoDeReserva(pagada)).isEqualTo(EstadoReservaNombre.FINALIZADA);
    }

    @Test
    void dadoUnDiaFinalizado_cuandoSeFinalizanReservas_entoncesNoTocaLasQueNoEstanPagadas() {
        UUID dia = crearDia(LocalDateTime.now().minusDays(5), EstadoActividadDiaNombre.FINALIZADA);
        UUID pendiente = crearReserva(dia, EstadoReservaNombre.PENDIENTE);
        UUID cancelada = crearReserva(dia, EstadoReservaNombre.CANCELADA_CON_REEMBOLSO);

        reservaService.finalizarReservas();

        assertThat(estadoDeReserva(pendiente)).isEqualTo(EstadoReservaNombre.PENDIENTE);
        assertThat(estadoDeReserva(cancelada)).isEqualTo(EstadoReservaNombre.CANCELADA_CON_REEMBOLSO);
    }

    // AUXILIARES

    private UUID crearDia(LocalDateTime inicio, EstadoActividadDiaNombre estado) {
        return crearDia(inicio, inicio.plusHours(3), estado);
    }

    private UUID crearDia(LocalDateTime inicio, LocalDateTime fin, EstadoActividadDiaNombre estado) {
        return transactionTemplate.execute(status -> {
            Actividad actividad = entityManager.find(Actividad.class, idActividad);

            ActividadDia dia = new ActividadDia();
            dia.setFechaHoraInicio(inicio);
            dia.setFechaHoraFin(fin);
            dia.setCuposMax(20);
            dia.cambiarEstado(catalogo.estadoActividadDia(estado),
                    LocalDateTime.now().minusDays(10), "Alta de prueba");

            actividad.addActividadDia(dia);
            entityManager.flush();
            return dia.getId();
        });
    }

    private UUID crearReserva(UUID idDia, EstadoReservaNombre estadoNombre) {
        return transactionTemplate.execute(status -> {
            EstadoReserva estado = reservaRepository
                    .findEstadoReservaByEstadoReservaNombre(estadoNombre)
                    .orElseThrow(() -> new IllegalStateException(
                            "EstadoReservaSeeder no creo el estado " + estadoNombre));

            Reserva reserva = new Reserva();
            reserva.setFechaHoraInicio(LocalDateTime.now());
            // Lejos en el futuro para que expirarReservas no toque las pendientes durante el test
            reserva.setFechaHoraExpiracion(LocalDateTime.now().plusDays(1));
            reserva.setTotalReserva(BigDecimal.valueOf(15000));
            reserva.setActividad(entityManager.find(Actividad.class, idActividad));
            reserva.setActividadDia(entityManager.find(ActividadDia.class, idDia));
            reserva.setVisitante(usuarios.visitante());
            reserva.cambiarEstado(estado, LocalDateTime.now());

            return reservaRepository.save(reserva).getId();
        });
    }

    private EstadoActividadDiaNombre estadoDelDia(UUID idDia) {
        return transactionTemplate.execute(status -> entityManager.find(ActividadDia.class, idDia)
                .getEstadoActual().getEstado().getNombre());
    }

    private EstadoReservaNombre estadoDeReserva(UUID idReserva) {
        return transactionTemplate.execute(status -> reservaRepository.findById(idReserva).orElseThrow()
                .getEstadoActual().getEstadoReserva().getNombre());
    }
}
