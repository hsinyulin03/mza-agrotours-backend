package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOCambioEstadoActividad;
import com.mza_agrotours.backend.dtos.administrador_sistemas.EstablecimientoSuspenderReq;
import com.mza_agrotours.backend.dtos.reservas.RealizarReservaDTO;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotActiveException;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureActividad;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import com.mza_agrotours.backend.support.FixtureUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Suspending an establecimiento only cuts new reservations and removes it from the general catalogs
 * (covered in EstablecimientoCatalogoVisitanteIT and LandingDestacadosIT). Its detail pages stay
 * reachable and the productor keeps operating its activities. These tests drive the real services
 * against a real database, which is the only place that cross-module contract is visible.
 */
@Transactional
class EstablecimientoSuspensionIT extends AbstractIntegrationTest {

    private static final String MOTIVO = "Incumplimiento de normativa";

    @Autowired
    private AdministradorSistemasService administradorSistemasService;

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private EstablecimientoService establecimientoService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private FixtureEstablecimiento establecimientos;

    @Autowired
    private FixtureActividad actividades;

    @Autowired
    private FixtureUsuario usuarios;

    private Establecimiento establecimiento;
    private AdministradorSistemas ejecutor;

    @BeforeEach
    void setUp() {
        this.establecimiento = establecimientos.establecimientoQueAdmiteReservas();
        this.ejecutor = usuarios.administrador();
    }

    @Test
    void dadoUnEstablecimiento_cuandoSeSuspende_entoncesPuedeCambiarElEstadoDeSusActividades() {
        Actividad actividad = actividades.actividadBorradorEn(establecimiento);
        suspender();

        DTOCambioEstadoActividad dto = new DTOCambioEstadoActividad();
        dto.setEstado(EstadoActividadNombre.PUBLICADO.name());

        assertThat(actividadService.cambiarEstadoActividad(establecimiento.getId(), actividad.getId(), dto)
                .getEstadoNuevo())
                .isEqualTo(EstadoActividadNombre.PUBLICADO);
    }

    @Test
    void dadoUnEstablecimiento_cuandoSeSuspende_entoncesPuedeDarDeBajaSusActividades() {
        Actividad actividad = actividades.actividadPublicadaEn(establecimiento);
        suspender();

        actividadService.darBajaActividad(establecimiento.getId(), actividad.getId());

        assertThat(actividad.getFechaHoraBaja()).isNotNull();
    }

    @Test
    void dadoUnEstablecimiento_cuandoSeSuspende_entoncesSuDetalleSigueVisible() {
        suspender();

        assertThat(establecimientoService.obtenerDetalleEstablecimientoVisitante(establecimiento.getId()))
                .isNotNull();
    }

    @Test
    void dadoUnEstablecimiento_cuandoSeSuspende_entoncesElDetalleDeSusActividadesNoAdmiteReservas() {
        Actividad actividad = actividades.actividadPublicadaEn(establecimiento);

        assertThat(actividadService.obtenerDetallePorId(actividad.getId()).isAdmiteReservas()).isTrue();

        suspender();

        assertThat(actividadService.obtenerDetallePorId(actividad.getId()).isAdmiteReservas()).isFalse();
    }

    @Test
    void dadoUnEstablecimiento_cuandoSeSuspende_entoncesNoPuedeReservarActividad() {
        Actividad actividad = actividades.actividadConDiaReservableEn(establecimiento);
        Visitante visitante = usuarios.visitante();
        RealizarReservaDTO reserva = new RealizarReservaDTO(
                actividad.getActividadesDias().get(0).getId().toString(), List.of());

        // Control: con el establecimiento activo la reserva falla mas adelante en el flujo,
        // nunca por actividad inactiva. Sin esto la asercion de abajo podria pasar por otro motivo.
        assertThatThrownBy(() -> reservaService
                .handleIniciarReserva(reserva, visitante.getUsuario().getEmail()))
                .isNotInstanceOf(ActividadNotActiveException.class);

        suspender();

        assertThatThrownBy(() -> reservaService
                .handleIniciarReserva(reserva, visitante.getUsuario().getEmail()))
                .isInstanceOf(ActividadNotActiveException.class);
    }

    @Test
    void dadoUnEstablecimientoSuspendido_cuandoSeReactiva_entoncesVuelveAAdmitirReservas() {
        Actividad actividad = actividades.actividadPublicadaEn(establecimiento);

        suspender();

        assertThat(actividadService.obtenerDetallePorId(actividad.getId()).isAdmiteReservas()).isFalse();

        reactivar();

        assertThat(actividadService.obtenerDetallePorId(actividad.getId()).isAdmiteReservas()).isTrue();
    }

    private void suspender() {
        EstablecimientoSuspenderReq req = new EstablecimientoSuspenderReq();
        req.setMotivo(MOTIVO);
        administradorSistemasService.suspenderEstablecimiento(
                establecimiento.getId(), req, ejecutor.getUsuario().getEmail());
    }

    private void reactivar() {
        administradorSistemasService.reactivarEstablecimiento(
                establecimiento.getId(), ejecutor.getUsuario().getEmail());
    }
}
