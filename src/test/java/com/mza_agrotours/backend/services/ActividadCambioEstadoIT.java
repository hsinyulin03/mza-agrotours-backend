package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOActividadesResponse;
import com.mza_agrotours.backend.dtos.actividad.DTOCambioEstadoActividad;
import com.mza_agrotours.backend.dtos.actividad.DTOCambioEstadoActividadResponse;
import com.mza_agrotours.backend.dtos.actividad.DTOReservasBloqueantes;
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
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.exceptions.actividad.ActividadError;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Switch de publicacion: el pase a borrador se bloquea cuando quedan reservas vivas de dias que
 * todavia no ocurrieron. Se prueban las dos puntas del mismo criterio, porque tienen que coincidir:
 * el candado que pinta el listado y la validacion que corre al tocar el switch. Si se separan, el
 * productor ve el switch habilitado y se come un 409, o al reves.
 */
@Transactional
class ActividadCambioEstadoIT extends AbstractIntegrationTest {

    @Autowired private ActividadService actividadService;
    @Autowired private ReservaRepository reservaRepository;

    @Autowired private FixtureEstablecimiento establecimientos;
    @Autowired private FixtureActividad actividades;
    @Autowired private FixtureUsuario usuarios;
    @Autowired private FixtureCatalogo catalogo;

    @PersistenceContext private EntityManager entityManager;

    private Establecimiento establecimiento;

    @BeforeEach
    void setUp() {
        this.establecimiento = establecimientos.establecimientoActivo();
    }

    // EL SWITCH

    @Test
    void publicaUnaActividadQueEstabaEnBorrador() {
        Actividad actividad = actividades.actividadBorradorEn(establecimiento);

        DTOCambioEstadoActividadResponse response = cambiarEstado(actividad, EstadoActividadNombre.PUBLICADO);

        assertThat(response.getActividadId()).isEqualTo(actividad.getId());
        assertThat(response.getEstadoAnterior()).isEqualTo(EstadoActividadNombre.BORRADOR);
        assertThat(response.getEstadoNuevo()).isEqualTo(EstadoActividadNombre.PUBLICADO);
        assertThat(actividad.getEstado().getNombre()).isEqualTo(EstadoActividadNombre.PUBLICADO);
    }

    /** Publicar nunca se restringe: tener reservas vivas no impide volver a publicar. */
    @Test
    void publicarNoSeBloqueaAunqueHayaReservasVivas() {
        Actividad actividad = actividades.actividadBorradorEn(establecimiento);
        ActividadDia dia = agregarDia(actividad, LocalDateTime.now().plusDays(3));
        crearReserva(actividad, dia, EstadoReservaNombre.PAGADA);

        cambiarEstado(actividad, EstadoActividadNombre.PUBLICADO);

        assertThat(actividad.getEstado().getNombre()).isEqualTo(EstadoActividadNombre.PUBLICADO);
    }

    @Test
    void rechazaElPaseABorradorSiHayReservasVivasDeDiasFuturos() {
        Actividad actividad = actividades.actividadPublicadaEn(establecimiento);
        ActividadDia dia = agregarDia(actividad, LocalDateTime.now().plusDays(3));
        crearReserva(actividad, dia, EstadoReservaNombre.PENDIENTE);
        crearReserva(actividad, dia, EstadoReservaNombre.PAGADA);
        crearReserva(actividad, dia, EstadoReservaNombre.PAGADA);

        assertThatThrownBy(() -> cambiarEstado(actividad, EstadoActividadNombre.BORRADOR))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ActividadError.ACTIVIDAD_CON_RESERVAS_ACTIVAS);

                    // El front necesita los numeros para el cartel del candado
                    DTOReservasBloqueantes data = (DTOReservasBloqueantes) appEx.getData();
                    assertThat(data.getPendientes()).isEqualTo(1);
                    assertThat(data.getPagadas()).isEqualTo(2);
                    assertThat(data.getTotal()).isEqualTo(3);

                    assertThat(appEx.getMessage()).contains("3 reservas");
                });

        // No quedo a medio camino
        assertThat(actividad.getEstado().getNombre()).isEqualTo(EstadoActividadNombre.PUBLICADO);
    }

    /**
     * Nadie pasa las reservas a FINALIZADA todavia, asi que una pagada de una visita que ya ocurrio
     * sigue en PAGADA para siempre. No debe trabar el switch de por vida.
     */
    @Test
    void permiteElPaseABorradorSiLasReservasSonDeDiasQueYaPasaron() {
        Actividad actividad = actividades.actividadPublicadaEn(establecimiento);
        ActividadDia diaPasado = agregarDia(actividad, LocalDateTime.now().minusDays(3));
        crearReserva(actividad, diaPasado, EstadoReservaNombre.PAGADA);

        cambiarEstado(actividad, EstadoActividadNombre.BORRADOR);

        assertThat(actividad.getEstado().getNombre()).isEqualTo(EstadoActividadNombre.BORRADOR);
    }

    @Test
    void lasReservasCanceladasYExpiradasNoTraban() {
        Actividad actividad = actividades.actividadPublicadaEn(establecimiento);
        ActividadDia dia = agregarDia(actividad, LocalDateTime.now().plusDays(3));
        crearReserva(actividad, dia, EstadoReservaNombre.EXPIRADA);
        crearReserva(actividad, dia, EstadoReservaNombre.CANCELADA_SIN_REEMBOLSO);
        crearReserva(actividad, dia, EstadoReservaNombre.CANCELADA_CON_REEMBOLSO);

        cambiarEstado(actividad, EstadoActividadNombre.BORRADOR);

        assertThat(actividad.getEstado().getNombre()).isEqualTo(EstadoActividadNombre.BORRADOR);
    }

    @Test
    void rechazaElCambioSiLaActividadEstaDadaDeBaja() {
        Actividad actividad = actividades.actividadBajadaEn(establecimiento);

        assertThatThrownBy(() -> cambiarEstado(actividad, EstadoActividadNombre.PUBLICADO))
                .isInstanceOf(ValidacionNegocioException.class)
                .hasMessageContaining("dada de baja");

        assertThat(actividad.getEstado().getNombre()).isEqualTo(EstadoActividadNombre.DADO_DE_BAJA);
    }

    @Test
    void rechazaElCambioAlEstadoEnElQueYaEsta() {
        Actividad actividad = actividades.actividadPublicadaEn(establecimiento);

        assertThatThrownBy(() -> cambiarEstado(actividad, EstadoActividadNombre.PUBLICADO))
                .isInstanceOf(ValidacionNegocioException.class)
                .hasMessageContaining("ya se encuentra");
    }

    /** La baja tiene su propio endpoint, que ademas cancela dias y reservas. Por aca no se cuela. */
    @Test
    void noPermiteDarDeBajaPorElSwitch() {
        Actividad actividad = actividades.actividadPublicadaEn(establecimiento);

        assertThatThrownBy(() -> cambiarEstado(actividad, EstadoActividadNombre.DADO_DE_BAJA))
                .isInstanceOf(ValidacionNegocioException.class)
                .hasMessageContaining("Borrador y Publicado");

        assertThat(actividad.getEstado().getNombre()).isEqualTo(EstadoActividadNombre.PUBLICADO);
        assertThat(actividad.getFechaHoraBaja()).isNull();
    }

    // EL CANDADO DEL LISTADO

    @Test
    void elListadoTraeLaCantidadDeReservasQueTrabanElSwitch() {
        Actividad actividad = actividades.actividadPublicadaEn(establecimiento);
        ActividadDia dia = agregarDia(actividad, LocalDateTime.now().plusDays(3));
        crearReserva(actividad, dia, EstadoReservaNombre.PENDIENTE);
        crearReserva(actividad, dia, EstadoReservaNombre.PAGADA);
        crearReserva(actividad, dia, EstadoReservaNombre.EXPIRADA);   // no cuenta

        DTOActividadesResponse fila = buscarEnListado(actividad);

        // Cuenta reservas, no personas: son 2 reservas vivas
        assertThat(fila.getCantidadReservasAsociadas()).isEqualTo(2L);
        assertThat(fila.isPuedeCambiarEstado()).isFalse();
    }

    @Test
    void elListadoNoCuentaLasReservasDeDiasQueYaPasaron() {
        Actividad actividad = actividades.actividadPublicadaEn(establecimiento);
        ActividadDia diaPasado = agregarDia(actividad, LocalDateTime.now().minusDays(3));
        crearReserva(actividad, diaPasado, EstadoReservaNombre.PAGADA);

        DTOActividadesResponse fila = buscarEnListado(actividad);

        assertThat(fila.getCantidadReservasAsociadas()).isZero();
        assertThat(fila.isPuedeCambiarEstado()).isTrue();
    }

    @Test
    void unaActividadSinReservasLlegaConElSwitchHabilitado() {
        Actividad actividad = actividades.actividadPublicadaEn(establecimiento);
        agregarDia(actividad, LocalDateTime.now().plusDays(3));

        DTOActividadesResponse fila = buscarEnListado(actividad);

        assertThat(fila.getCantidadReservasAsociadas()).isZero();
        assertThat(fila.isPuedeCambiarEstado()).isTrue();
    }

    /**
     * Las dadas de baja siguen apareciendo en el listado (findByFiltrosDinamicos las manda al final),
     * pero su estado es terminal: el switch va deshabilitado aunque no tengan ninguna reserva.
     */
    @Test
    void elSwitchLlegaDeshabilitadoEnLasActividadesDadasDeBaja() {
        Actividad actividad = actividades.actividadBajadaEn(establecimiento);

        DTOActividadesResponse fila = buscarEnListado(actividad);

        assertThat(fila.getCantidadReservasAsociadas()).isZero();
        assertThat(fila.isPuedeCambiarEstado()).isFalse();
    }

    /**
     * Estado que hoy no se alcanza, porque sobre una actividad en borrador nadie puede reservar.
     * La condicion existe igual en el servicio y se fija aca: publicar no debe quedar trabado si
     * alguna vez se permite reservar sobre una actividad despublicada.
     */
    @Test
    void enBorradorElSwitchQuedaHabilitadoAunqueHayaReservasVivas() {
        Actividad actividad = actividades.actividadBorradorEn(establecimiento);
        ActividadDia dia = agregarDia(actividad, LocalDateTime.now().plusDays(3));
        crearReserva(actividad, dia, EstadoReservaNombre.PAGADA);

        DTOActividadesResponse fila = buscarEnListado(actividad);

        assertThat(fila.getCantidadReservasAsociadas()).isEqualTo(1L);
        assertThat(fila.isPuedeCambiarEstado()).isTrue();
    }

    @Test
    void elListadoCuentaCadaActividadPorSeparado() {
        Actividad conReservas = actividades.actividadPublicadaEn(establecimiento);
        Actividad sinReservas = actividades.actividadPublicadaEn(establecimiento);

        ActividadDia dia = agregarDia(conReservas, LocalDateTime.now().plusDays(3));
        crearReserva(conReservas, dia, EstadoReservaNombre.PAGADA);

        // Una sola query agrupada resuelve toda la pagina: los conteos no se mezclan
        assertThat(buscarEnListado(conReservas).getCantidadReservasAsociadas()).isEqualTo(1L);
        assertThat(buscarEnListado(sinReservas).getCantidadReservasAsociadas()).isZero();
    }

    // AUXILIARES

    private DTOCambioEstadoActividadResponse cambiarEstado(Actividad actividad, EstadoActividadNombre destino) {
        DTOCambioEstadoActividad dto = new DTOCambioEstadoActividad();
        dto.setEstado(destino.name());
        return actividadService.cambiarEstadoActividad(establecimiento.getId(), actividad.getId(), dto);
    }

    private DTOActividadesResponse buscarEnListado(Actividad actividad) {
        return actividadService
                .obtenerListadoActividades(establecimiento.getId(), null, null, PageRequest.of(0, 50))
                .getContent().stream()
                .filter(fila -> fila.getId().equals(actividad.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "La actividad " + actividad.getId() + " no vino en el listado"));
    }

    /**
     * El dia se persiste por cascada desde la actividad, que ya esta managed. Se usa flush y no
     * save() porque save() sobre una entidad con id hace merge, y el merge devuelve copias: la
     * instancia del test quedaria transient y no veria lo que escribe el servicio.
     */
    private ActividadDia agregarDia(Actividad actividad, LocalDateTime inicio) {
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

    private Reserva crearReserva(Actividad actividad, ActividadDia dia, EstadoReservaNombre estadoNombre) {
        Visitante visitante = usuarios.visitante();
        EstadoReserva estado = reservaRepository
                .findEstadoReservaByEstadoReservaNombre(estadoNombre)
                .orElseThrow(() -> new IllegalStateException(
                        "EstadoReservaSeeder no creo el estado " + estadoNombre));

        Reserva reserva = new Reserva();
        reserva.setFechaHoraInicio(LocalDateTime.now());
        reserva.setTotalReserva(BigDecimal.valueOf(15000));
        reserva.setActividad(actividad);
        reserva.setActividadDia(dia);
        reserva.setVisitante(visitante);
        reserva.cambiarEstado(estado, LocalDateTime.now());

        return reservaRepository.saveAndFlush(reserva);
    }
}
