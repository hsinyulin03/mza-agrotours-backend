package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOActividadAlta;
import com.mza_agrotours.backend.dtos.actividad.DTOActividadUpdate;
import com.mza_agrotours.backend.dtos.administrador_sistemas.EstablecimientoSuspenderReq;
import com.mza_agrotours.backend.dtos.reservas.RealizarReservaDTO;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.exceptions.EstablecimientoError;
import com.mza_agrotours.backend.exceptions.ResourceNotFoundException;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotActiveException;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Suspending an establecimiento is an invariant that lives across modules: the state changes in
 * AdministradorSistemasService and every other module has to stop serving it. These tests drive
 * the real services against a real database, which is the only place that contract is visible.
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
    private Fixtures fixtures;

    private Establecimiento establecimiento;
    private AdministradorSistemas ejecutor;

    @BeforeEach
    void setUp() {
        this.establecimiento = fixtures.establecimientoActivo();
        this.ejecutor = fixtures.administrador();
    }

    @Test
    void dadoUnEstablecimiento_cuandoSeSuspende_entoncesNoPuedeCrearActividad() {
        suspender();

        assertThatThrownBy(() -> actividadService.altaActividad(
                establecimiento.getId(), new DTOActividadAlta()))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", EstablecimientoError.ESTABLECIMIENTO_SUSPENDIDO);
    }

    @Test
    void dadoUnEstablecimiento_cuandoSeSuspende_entoncesNoPuedeModificarActividad() {
        Actividad actividad = fixtures.actividadPublicadaEn(establecimiento);
        suspender();

        assertThatThrownBy(() -> actividadService.modificarActividad(
                establecimiento.getId(), actividad.getId(), new DTOActividadUpdate()))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", EstablecimientoError.ESTABLECIMIENTO_SUSPENDIDO);
    }

    @Test
    void dadoUnEstablecimiento_cuandoSeSuspende_entoncesNoEsVisibleSuDetalle() {
        assertThat(establecimientoService.obtenerDetalleEstablecimientoVisitante(establecimiento.getId()))
                .isNotNull();

        suspender();

        assertThatThrownBy(() -> establecimientoService
                .obtenerDetalleEstablecimientoVisitante(establecimiento.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void dadoUnEstablecimiento_cuandoSeSuspende_entoncesNoEsVisibleSuListaDeActividades() {
        Actividad actividad = fixtures.actividadPublicadaEn(establecimiento);

        assertThat(actividadService.obtenerDetallePorId(actividad.getId())).isNotNull();

        suspender();

        assertThatThrownBy(() -> actividadService.obtenerDetallePorId(actividad.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void dadoUnEstablecimiento_cuandoSeSuspende_entoncesNoPuedeReservarActividad() {
        Actividad actividad = fixtures.actividadConDiaReservableEn(establecimiento);
        Visitante visitante = fixtures.visitante();
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
    void dadoUnEstablecimientoSuspendido_cuandoSeReactiva_entoncesVuelveATodosLosModulos() {
        Actividad actividad = fixtures.actividadPublicadaEn(establecimiento);

        suspender();

        assertThatThrownBy(() -> establecimientoService
                .obtenerDetalleEstablecimientoVisitante(establecimiento.getId()))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> actividadService.obtenerDetallePorId(actividad.getId()))
                .isInstanceOf(ResourceNotFoundException.class);

        reactivar();

        assertThat(establecimientoService.obtenerDetalleEstablecimientoVisitante(establecimiento.getId()))
                .isNotNull();
        assertThat(actividadService.obtenerDetallePorId(actividad.getId())).isNotNull();
        // Ya no corta por suspension: ahora falla en la validacion del DTO vacio, que es otra cosa.
        assertThatThrownBy(() -> actividadService.altaActividad(
                establecimiento.getId(), new DTOActividadAlta()))
                .isNotInstanceOf(AppException.class);
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
