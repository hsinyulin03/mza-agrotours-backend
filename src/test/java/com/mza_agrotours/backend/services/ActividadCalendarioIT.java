package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOActividadDiaResponse;
import com.mza_agrotours.backend.dtos.actividad.DTOCalendarioActividadDiaResponse;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.actividad.ActividadRangoEtario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.entities.reservas.ReservaDetalle;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.repositories.ReservaRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-ACT-07: detalle del calendario. Lo que se prueba acá es que las métricas cuenten PERSONAS y no
 * reservas, que cada estado caiga en su contador, y que el mes consultado acote los días devueltos.
 * Son tres cosas que compilan igual estando mal y sólo se ven contra datos reales.
 */
@Transactional
class ActividadCalendarioIT extends AbstractIntegrationTest {

    private static final int CUPOS_MAX = 20;

    @Autowired private ActividadService actividadService;
    @Autowired private ReservaRepository reservaRepository;

    @Autowired private FixtureEstablecimiento establecimientos;
    @Autowired private FixtureActividad actividades;
    @Autowired private FixtureUsuario usuarios;
    @Autowired private FixtureCatalogo catalogo;

    @PersistenceContext private EntityManager entityManager;

    private Actividad actividad;
    private ActividadRangoEtario rangoEtario;

    /** Mes que van a consultar los tests. Se elige uno futuro para no chocar con las validaciones. */
    private YearMonth mesConsultado;

    @BeforeEach
    void setUp() {
        Establecimiento establecimiento = establecimientos.establecimientoActivo();
        this.actividad = actividades.actividadPublicadaEn(establecimiento);
        this.mesConsultado = YearMonth.from(LocalDate.now().plusMonths(1));

        this.rangoEtario = new ActividadRangoEtario();
        rangoEtario.setNombre("General");
        rangoEtario.setPrecio(BigDecimal.valueOf(5000));
        rangoEtario.setEdadMinima(0);
        rangoEtario.setEdadMaxima(99);
        rangoEtario.setEsTarifaBase(true);
        actividad.addActividadRangoEtario(rangoEtario);

        entityManager.flush();
    }

    @Test
    void cuentaPersonasYNoReservas() {
        ActividadDia dia = agregarDia(mesConsultado.atDay(10).atTime(6, 0),
                                      mesConsultado.atDay(10).atTime(11, 0));

        crearReserva(dia, EstadoReservaNombre.PENDIENTE, 1);
        crearReserva(dia, EstadoReservaNombre.PAGADA, 3);

        DTOActividadDiaResponse dtoDia = consultarDia(dia);

        // Dos reservas, cuatro personas. Si contara reservas, serían 1 y 1.
        assertThat(dtoDia.getCuposPendientes()).isEqualTo(1);
        assertThat(dtoDia.getCuposPagados()).isEqualTo(3);
        assertThat(dtoDia.getCuposMaximos()).isEqualTo(CUPOS_MAX);
        assertThat(dtoDia.getCuposLibres()).isEqualTo(CUPOS_MAX - 4);
    }

    @Test
    void ignoraLasReservasQueNoOcupanCupo() {
        ActividadDia dia = agregarDia(mesConsultado.atDay(10).atTime(6, 0),
                                      mesConsultado.atDay(10).atTime(11, 0));

        crearReserva(dia, EstadoReservaNombre.PENDIENTE, 1);
        crearReserva(dia, EstadoReservaNombre.PAGADA, 3);
        crearReserva(dia, EstadoReservaNombre.EXPIRADA, 5);
        crearReserva(dia, EstadoReservaNombre.CANCELADA_SIN_REEMBOLSO, 4);

        DTOActividadDiaResponse dtoDia = consultarDia(dia);

        // Las expiradas y canceladas liberaron su cupo: no entran en la barra.
        assertThat(dtoDia.getCuposPendientes()).isEqualTo(1);
        assertThat(dtoDia.getCuposPagados()).isEqualTo(3);
        assertThat(dtoDia.getCuposLibres()).isEqualTo(CUPOS_MAX - 4);
    }

    @Test
    void unDiaSinReservasLlegaEnCeroYConTodosLosCuposLibres() {
        ActividadDia dia = agregarDia(mesConsultado.atDay(10).atTime(6, 0),
                                      mesConsultado.atDay(10).atTime(11, 0));

        DTOActividadDiaResponse dtoDia = consultarDia(dia);

        // La consulta agrupada no devuelve fila para este día: aplicarCupos recibe null.
        assertThat(dtoDia.getCuposPendientes()).isZero();
        assertThat(dtoDia.getCuposPagados()).isZero();
        assertThat(dtoDia.getCuposLibres()).isEqualTo(CUPOS_MAX);
    }

    @Test
    void devuelveSoloLosDiasDelMesConsultado() {
        ActividadDia delMes = agregarDia(mesConsultado.atDay(10).atTime(6, 0),
                                         mesConsultado.atDay(10).atTime(11, 0));
        ActividadDia delMesSiguiente = agregarDia(mesConsultado.plusMonths(1).atDay(5).atTime(6, 0),
                                                  mesConsultado.plusMonths(1).atDay(5).atTime(11, 0));

        List<DTOActividadDiaResponse> dias = consultarCalendario().getDiasDelMes();

        assertThat(dias).extracting(DTOActividadDiaResponse::getId)
                .containsExactly(delMes.getId())
                .doesNotContain(delMesSiguiente.getId());
    }

    @Test
    void exponeElHorarioDelDiaSinFormatear() {
        ActividadDia dia = agregarDia(mesConsultado.atDay(10).atTime(6, 0),
                                      mesConsultado.atDay(10).atTime(11, 0));

        DTOActividadDiaResponse dtoDia = consultarDia(dia);

        assertThat(dtoDia.getFecha()).isEqualTo(mesConsultado.atDay(10));
        assertThat(dtoDia.getHoraInicio()).isEqualTo(LocalTime.of(6, 0));
        assertThat(dtoDia.getHoraFin()).isEqualTo(LocalTime.of(11, 0));
        assertThat(dtoDia.getEstadoActual()).isEqualTo(EstadoActividadDiaNombre.ACTIVA.name());
    }

    /**
     * Las métricas globales suman toda la historia de la actividad, sin importar el mes consultado.
     * Ojo: hoy nada en producción pasa una reserva a FINALIZADA (falta la US del demonio), así que
     * esa card va a mostrar cero hasta que exista. Acá se fuerza el estado para probar el contador.
     */
    @Test
    void lasMetricasGlobalesSumanTodaLaActividad() {
        ActividadDia delMes = agregarDia(mesConsultado.atDay(10).atTime(6, 0),
                                         mesConsultado.atDay(10).atTime(11, 0));
        ActividadDia delMesSiguiente = agregarDia(mesConsultado.plusMonths(1).atDay(5).atTime(6, 0),
                                                  mesConsultado.plusMonths(1).atDay(5).atTime(11, 0));

        crearReserva(delMes, EstadoReservaNombre.PENDIENTE, 1);
        crearReserva(delMes, EstadoReservaNombre.PAGADA, 3);
        crearReserva(delMes, EstadoReservaNombre.FINALIZADA, 2);
        crearReserva(delMesSiguiente, EstadoReservaNombre.PAGADA, 4);   // otro mes: suma igual

        DTOCalendarioActividadDiaResponse dto = consultarCalendario();

        assertThat(dto.getMetricas().getPersonasPendientes()).isEqualTo(1);
        assertThat(dto.getMetricas().getPersonasPagadas()).isEqualTo(7);   // 3 + 4
        assertThat(dto.getMetricas().getPersonasFinalizadas()).isEqualTo(2);

        // Y la barra del día sólo mira ese día
        DTOActividadDiaResponse dtoDia = buscarDia(dto, delMes);
        assertThat(dtoDia.getCuposPagados()).isEqualTo(3);
    }

    @Test
    void sinReservasLasMetricasLleganEnCero() {
        agregarDia(mesConsultado.atDay(10).atTime(6, 0), mesConsultado.atDay(10).atTime(11, 0));

        DTOCalendarioActividadDiaResponse dto = consultarCalendario();

        assertThat(dto.getMetricas()).isNotNull();
        assertThat(dto.getMetricas().getPersonasPendientes()).isZero();
        assertThat(dto.getMetricas().getPersonasPagadas()).isZero();
        assertThat(dto.getMetricas().getPersonasFinalizadas()).isZero();
    }

    // AUXILIARES

    private DTOCalendarioActividadDiaResponse consultarCalendario() {
        return actividadService.obtenerDetalleCalendario(
                actividad.getId(), mesConsultado.getMonthValue(), mesConsultado.getYear());
    }

    private DTOActividadDiaResponse consultarDia(ActividadDia dia) {
        return buscarDia(consultarCalendario(), dia);
    }

    private DTOActividadDiaResponse buscarDia(DTOCalendarioActividadDiaResponse dto, ActividadDia dia) {
        return dto.getDiasDelMes().stream()
                .filter(d -> d.getId().equals(dia.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("El día " + dia.getId() + " no vino en el calendario"));
    }

    /**
     * El día se persiste por cascada desde la actividad, que ya está managed. Se usa flush y no
     * save(), porque save() sobre una entidad con id hace merge y el merge devuelve copias: la
     * instancia del test quedaría transient.
     */
    private ActividadDia agregarDia(LocalDateTime inicio, LocalDateTime fin) {
        ActividadDia dia = new ActividadDia();
        dia.setFechaHoraInicio(inicio);
        dia.setFechaHoraFin(fin);
        dia.setCuposMax(CUPOS_MAX);
        dia.cambiarEstado(catalogo.estadoActividadDia(EstadoActividadDiaNombre.ACTIVA),
                          LocalDateTime.now(),
                          "Alta de la actividad");

        actividad.addActividadDia(dia);
        entityManager.flush();
        return dia;
    }

    private Reserva crearReserva(ActividadDia dia, EstadoReservaNombre estadoNombre, int cantidadPersonas) {
        Visitante visitante = usuarios.visitante();
        EstadoReserva estado = reservaRepository
                .findEstadoReservaByEstadoReservaNombre(estadoNombre)
                .orElseThrow(() -> new IllegalStateException(
                        "EstadoReservaSeeder no creó el estado " + estadoNombre));

        Reserva reserva = new Reserva();
        reserva.setFechaHoraInicio(LocalDateTime.now());
        reserva.setTotalReserva(BigDecimal.valueOf(5000L * cantidadPersonas));
        reserva.setActividad(actividad);
        reserva.setActividadDia(dia);
        reserva.setVisitante(visitante);
        reserva.cambiarEstado(estado, LocalDateTime.now());

        List<ReservaDetalle> detalles = new ArrayList<>();
        for (int renglon = 1; renglon <= cantidadPersonas; renglon++) {
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
