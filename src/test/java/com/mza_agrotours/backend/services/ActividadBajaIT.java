package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOBajaActividadResponse;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.ResourceNotFoundException;
import com.mza_agrotours.backend.exceptions.actividad.ActividadError;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-ACT-05: baja de actividad. La baja cruza tres agregados (actividad, actividad_dia y reserva),
 * asi que solo se ve completa contra una base real.
 */
@Transactional
class ActividadBajaIT extends AbstractIntegrationTest {

    @Autowired private ActividadService actividadService;
    @Autowired private ActividadRepository actividadRepository;
    @Autowired private ReservaRepository reservaRepository;

    @Autowired private FixtureEstablecimiento establecimientos;
    @Autowired private FixtureActividad actividades;
    @Autowired private FixtureUsuario usuarios;
    @Autowired private FixtureCatalogo catalogo;

    @PersistenceContext private EntityManager entityManager;

    private Establecimiento establecimiento;
    private Actividad actividad;

    @BeforeEach
    void setUp() {
        this.establecimiento = establecimientos.establecimientoActivo();
        this.actividad = actividades.actividadPublicadaEn(establecimiento);
    }

    @Test
    void daDeBajaLaActividadYCancelaSusDiasFuturos() {
        ActividadDia diaFuturo = agregarDia(LocalDateTime.now().plusDays(3));
        ActividadDia diaPasado = agregarDia(LocalDateTime.now().minusDays(3));

        DTOBajaActividadResponse response = actividadService.darBajaActividad(
                establecimiento.getId(), actividad.getId());

        assertThat(response.getIdActividad()).isEqualTo(actividad.getId());
        assertThat(response.getEstado()).isEqualTo(EstadoActividadNombre.DADO_DE_BAJA.getNombre());

        assertThat(actividad.getEstado().getNombre()).isEqualTo(EstadoActividadNombre.DADO_DE_BAJA);
        assertThat(actividad.getFechaHoraBaja()).isNotNull();

        // El dia futuro queda cancelado y dado de baja
        assertThat(diaFuturo.getFechaHoraBaja()).isNotNull();
        assertThat(diaFuturo.getEstadoActual().getEstado().getNombre())
                .isEqualTo(EstadoActividadDiaNombre.CANCELADA);
        assertThat(diaFuturo.getEstadoActual().getMotivo()).isEqualTo("Baja de la actividad");
        assertThat(diaFuturo.getEstados()).hasSize(2);

        // El estado anterior quedo cerrado
        assertThat(diaFuturo.getEstados().get(0).getFechaHoraFin()).isNotNull();

        // Un dia que ya paso no se toca
        assertThat(diaPasado.getFechaHoraBaja()).isNull();
        assertThat(diaPasado.getEstadoActual().getEstado().getNombre())
                .isEqualTo(EstadoActividadDiaNombre.ACTIVA);
    }

    @Test
    void cancelaLasReservasPendientesSinReembolso() {
        ActividadDia dia = agregarDia(LocalDateTime.now().plusDays(3));
        Reserva pendiente = crearReserva(dia, EstadoReservaNombre.PENDIENTE);

        actividadService.darBajaActividad(establecimiento.getId(), actividad.getId());

        assertThat(pendiente.getEstadoActual().getEstadoReserva().getNombre())
                .isEqualTo(EstadoReservaNombre.CANCELADA_SIN_REEMBOLSO);
        assertThat(pendiente.getFechaHoraExpiracion()).isNull();
        assertThat(pendiente.getFechaHoraFin()).isNotNull();   // es un estado final
        assertThat(pendiente.getEstados()).hasSize(2);
    }

    @Test
    void rechazaLaBajaSiHayReservasPagadasDeDiasFuturos() {
        ActividadDia dia = agregarDia(LocalDateTime.now().plusDays(3));
        crearReserva(dia, EstadoReservaNombre.PAGADA);

        assertThatThrownBy(() -> actividadService.darBajaActividad(
                establecimiento.getId(), actividad.getId()))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ActividadError.ACTIVIDAD_CON_RESERVAS_PAGADAS));

        // Nada quedo a medio camino
        assertThat(actividad.getEstado().getNombre()).isEqualTo(EstadoActividadNombre.PUBLICADO);
        assertThat(actividad.getFechaHoraBaja()).isNull();
        assertThat(dia.getFechaHoraBaja()).isNull();
    }

    /**
     * Nadie pasa las reservas a FINALIZADA todavia, asi que una pagada de una visita que ya ocurrio
     * sigue en PAGADA para siempre. No debe bloquear la baja: esa visita ya se presto y se cobro.
     */
    @Test
    void permiteLaBajaSiLasReservasPagadasSonDeDiasQueYaPasaron() {
        ActividadDia diaPasado = agregarDia(LocalDateTime.now().minusDays(3));
        Reserva pagadaVieja = crearReserva(diaPasado, EstadoReservaNombre.PAGADA);

        actividadService.darBajaActividad(establecimiento.getId(), actividad.getId());

        assertThat(actividad.getEstado().getNombre()).isEqualTo(EstadoActividadNombre.DADO_DE_BAJA);
        // La reserva vieja no se toca: no es pendiente
        assertThat(pagadaVieja.getEstadoActual().getEstadoReserva().getNombre())
                .isEqualTo(EstadoReservaNombre.PAGADA);
    }

    @Test
    void fallaSiLaActividadNoExiste() {
        assertThatThrownBy(() -> actividadService.darBajaActividad(
                establecimiento.getId(), UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // AUXILIARES

    /**
     * El dia se persiste por cascada desde la actividad, que ya esta managed. Se usa flush y no
     * save() porque save() sobre una entidad con id hace merge, y el merge devuelve copias: la
     * instancia que tendria el test quedaria transient y no veria lo que escribe el servicio.
     */
    private ActividadDia agregarDia(LocalDateTime inicio) {
        ActividadDia dia = new ActividadDia();
        dia.setFechaHoraInicio(inicio);
        dia.setFechaHoraFin(inicio.plusHours(3));
        dia.setCuposMax(20);
        dia.cambiarEstado(catalogo.estadoActividadDia(EstadoActividadDiaNombre.ACTIVA),
                LocalDateTime.now().minusDays(10), "Alta de prueba");

        actividad.addActividadDia(dia);
        entityManager.flush();
        return dia;
    }

    private Reserva crearReserva(ActividadDia dia, EstadoReservaNombre estadoNombre) {
        Visitante visitante = usuarios.visitante();
        EstadoReserva estado = reservaRepository
                .findEstadoReservaByEstadoReservaNombre(estadoNombre)
                .orElseThrow(() -> new IllegalStateException(
                        "EstadoReservaSeeder no creo el estado " + estadoNombre));

        Reserva reserva = new Reserva();
        reserva.setFechaHoraInicio(LocalDateTime.now());
        reserva.setFechaHoraExpiracion(LocalDateTime.now().plusMinutes(15));
        reserva.setTotalReserva(BigDecimal.valueOf(15000));
        reserva.setActividad(actividad);
        reserva.setActividadDia(dia);
        reserva.setVisitante(visitante);
        reserva.cambiarEstado(estado, LocalDateTime.now());

        return reservaRepository.saveAndFlush(reserva);
    }
}
