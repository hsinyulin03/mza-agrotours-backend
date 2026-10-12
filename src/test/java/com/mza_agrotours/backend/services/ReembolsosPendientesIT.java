package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.pago.ReembolsoGetCatalogoDTO;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.actividad.ActividadRangoEtario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.entities.reservas.ReservaDetalle;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoReembolsoNombre;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoReembolsoRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-RESE-07: listado de reembolsos pendientes del productor. Solo entran los "Pedido" del establecimiento,
 * con los cupos contados por persona.
 */
@Transactional
class ReembolsosPendientesIT extends AbstractIntegrationTest {

    @Autowired private ReservaService reservaService;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private ReembolsoRepository reembolsoRepository;
    @Autowired private EstadoReembolsoRepository estadoReembolsoRepository;

    @Autowired private FixtureEstablecimiento establecimientos;
    @Autowired private FixtureActividad actividades;
    @Autowired private FixtureUsuario usuarios;
    @Autowired private FixtureCatalogo catalogo;

    @PersistenceContext private EntityManager entityManager;

    private Establecimiento establecimiento;
    private Establecimiento otroEstablecimiento;

    @BeforeEach
    void setUp() {
        this.establecimiento = establecimientos.establecimientoActivo();
        this.otroEstablecimiento = establecimientos.establecimientoActivo();
    }

    @Test
    void listaSoloLosPedidosDelEstablecimientoConSusCupos() {
        LocalDateTime hace3Dias = LocalDateTime.now().minusDays(3).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime hace1Dia = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS);

        Reembolso masViejo = crearReembolso(establecimiento, EstadoReembolsoNombre.PEDIDO, hace3Dias, 3);
        Reembolso masNuevo = crearReembolso(establecimiento, EstadoReembolsoNombre.PEDIDO, hace1Dia, 1);
        crearReembolso(establecimiento, EstadoReembolsoNombre.EN_PROCESO, hace1Dia, 2);
        crearReembolso(otroEstablecimiento, EstadoReembolsoNombre.PEDIDO, hace1Dia, 2);

        Page<ReembolsoGetCatalogoDTO> pagina = reservaService.listarReembolsosPendientes(establecimiento.getId(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "fechaHoraPedido", "id")));

        assertThat(pagina.getTotalElements()).isEqualTo(2);
        assertThat(pagina.getContent()).extracting(ReembolsoGetCatalogoDTO::getReembolsoId)
                .containsExactly(masViejo.getId(), masNuevo.getId());

        ReembolsoGetCatalogoDTO primero = pagina.getContent().get(0);
        assertThat(primero.getReservaId()).isEqualTo(masViejo.getReserva().getId());
        assertThat(primero.getCuposReservados()).isEqualTo(3);
        assertThat(primero.getFechaHoraPedido()).isEqualTo(hace3Dias);
        assertThat(primero.getMontoReembolso()).isEqualByComparingTo("15000");
        assertThat(primero.getNombreActividad()).isNotBlank();
        assertThat(primero.getEmailVisitante()).isNotBlank();
    }

    @Test
    void paginaYCuentaSoloLosPedidos() {
        for (int i = 0; i < 3; i++)
            crearReembolso(establecimiento, EstadoReembolsoNombre.PEDIDO, LocalDateTime.now().minusDays(i), 1);
        crearReembolso(establecimiento, EstadoReembolsoNombre.REEMBOLSADO_PRODUCTOR, LocalDateTime.now(), 1);

        Page<ReembolsoGetCatalogoDTO> pagina = reservaService.listarReembolsosPendientes(establecimiento.getId(),
                PageRequest.of(0, 2, Sort.by("fechaHoraPedido")));

        assertThat(pagina.getContent()).hasSize(2);
        assertThat(pagina.getTotalElements()).isEqualTo(3);
    }

    // AUXILIARES

    private Reembolso crearReembolso(Establecimiento est, EstadoReembolsoNombre estado, LocalDateTime pedido, int personas) {
        Reserva reserva = crearReserva(est, personas);

        Reembolso reembolso = new Reembolso();
        reembolso.setReserva(reserva);
        reembolso.setMontoReembolso(reserva.getTotalReserva());
        reembolso.setFechaHoraPedido(pedido);
        reembolso.cambiarEstado(estadoReembolsoRepository.findByNombre(estado).orElseThrow(), pedido);
        return reembolsoRepository.saveAndFlush(reembolso);
    }

    private Reserva crearReserva(Establecimiento est, int personas) {
        Actividad actividad = actividades.actividadPublicadaEn(est);

        ActividadRangoEtario rangoEtario = new ActividadRangoEtario();
        rangoEtario.setNombre("General");
        rangoEtario.setPrecio(BigDecimal.valueOf(5000));
        rangoEtario.setEdadMinima(0);
        rangoEtario.setEdadMaxima(99);
        rangoEtario.setEsTarifaBase(true);
        actividad.addActividadRangoEtario(rangoEtario);

        ActividadDia dia = new ActividadDia();
        dia.setFechaHoraInicio(LocalDateTime.now().plusDays(20));
        dia.setFechaHoraFin(LocalDateTime.now().plusDays(20).plusHours(3));
        dia.setCuposMax(20);
        dia.cambiarEstado(catalogo.estadoActividadDia(EstadoActividadDiaNombre.ACTIVA), LocalDateTime.now(), "Alta de la actividad");
        actividad.addActividadDia(dia);
        entityManager.flush();

        Visitante visitante = usuarios.visitante();
        Reserva reserva = new Reserva();
        reserva.setFechaHoraInicio(LocalDateTime.now());
        reserva.setTotalReserva(BigDecimal.valueOf(5000L * personas));
        reserva.setActividad(actividad);
        reserva.setActividadDia(dia);
        reserva.setVisitante(visitante);
        reserva.cambiarEstado(reservaRepository.findEstadoReservaByEstadoReservaNombre(EstadoReservaNombre.CANCELADA_REEMBOLSO_PENDIENTE)
                .orElseThrow(), LocalDateTime.now());

        List<ReservaDetalle> detalles = new ArrayList<>();
        for (int renglon = 1; renglon <= personas; renglon++) {
            ReservaDetalle detalle = new ReservaDetalle();
            detalle.setRenglon(renglon);
            detalle.setNombre("Visitante " + renglon);
            detalle.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            detalle.setIdentificacion(String.format("%08d", renglon));
            detalle.setSubtotal(BigDecimal.valueOf(5000));
            detalle.setTipoIdentificacion(catalogo.tipoIdentificacion());
            detalle.setActividadRangoEtario(rangoEtario);
            detalles.add(detalle);
        }
        reserva.setReservaDetalles(detalles);

        return reservaRepository.saveAndFlush(reserva);
    }
}
