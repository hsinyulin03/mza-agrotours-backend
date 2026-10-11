package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.administrador_sistemas.EstablecimientoSuspenderReq;
import com.mza_agrotours.backend.dtos.reservas.RealizarReservaDTO;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.EstadoCobro;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotActiveException;
import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureActividad;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import com.mza_agrotours.backend.support.FixtureUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Un establecimiento sin cuenta de MP utilizable sigue visible, pero no admite reservas nuevas.
 */
@Transactional
class EstablecimientoCobroIT extends AbstractIntegrationTest {

    @Autowired private ActividadService actividadService;
    @Autowired private ReservaService reservaService;
    @Autowired private CuentaMercadoPagoService cuentaMercadoPagoService;
    @Autowired private AdministradorSistemasService administradorSistemasService;
    @Autowired private FixtureEstablecimiento establecimientos;
    @Autowired private FixtureActividad actividades;
    @Autowired private FixtureUsuario usuarios;

    @Test
    void dadoEstablecimientoSinCuenta_entoncesSeVePeroNoAdmiteReservas() {
        Establecimiento establecimiento = establecimientos.establecimientoActivo();
        Actividad actividad = actividades.actividadConDiaReservableEn(establecimiento);
        Visitante visitante = usuarios.visitante();

        assertThat(cuentaMercadoPagoService.obtenerEstado(establecimiento.getId()).estadoCobro())
                .isEqualTo(EstadoCobro.SIN_CUENTA);
        assertThat(actividadService.obtenerDetallePorId(actividad.getId()).isAdmiteReservas()).isFalse();

        assertThatThrownBy(() -> actividadService.getInfoParaReservar(
                actividad.getId(), visitante.getUsuario().getEmail()))
                .isInstanceOf(ActividadNotActiveException.class);
        assertThatThrownBy(() -> reservaService.handleIniciarReserva(
                reservaDe(actividad), visitante.getUsuario().getEmail()))
                .isInstanceOf(ActividadNotActiveException.class);
    }

    @Test
    void dadoEstablecimientoConCuentaVigente_entoncesAdmiteReservas() {
        Establecimiento establecimiento = establecimientos.establecimientoQueAdmiteReservas();
        Actividad actividad = actividades.actividadConDiaReservableEn(establecimiento);
        Visitante visitante = usuarios.visitante();

        assertThat(cuentaMercadoPagoService.obtenerEstado(establecimiento.getId()).estadoCobro())
                .isEqualTo(EstadoCobro.HABILITADO);
        assertThat(actividadService.obtenerDetallePorId(actividad.getId()).isAdmiteReservas()).isTrue();

        // Falla más adelante por el DTO sin detalles, nunca por no admitir reservas
        assertThatThrownBy(() -> reservaService.handleIniciarReserva(
                reservaDe(actividad), visitante.getUsuario().getEmail()))
                .isNotInstanceOf(ActividadNotActiveException.class);
    }

    @Test
    void dadoEstablecimientoConTokenVencido_entoncesNoAdmiteReservas() {
        Establecimiento establecimiento = establecimientos.conCuentaMercadoPago(
                establecimientos.establecimientoActivo(), LocalDateTime.now().minusDays(1));
        Actividad actividad = actividades.actividadConDiaReservableEn(establecimiento);

        assertThat(cuentaMercadoPagoService.obtenerEstado(establecimiento.getId()).estadoCobro())
                .isEqualTo(EstadoCobro.TOKEN_VENCIDO);
        assertThat(actividadService.obtenerDetallePorId(actividad.getId()).isAdmiteReservas()).isFalse();
    }

    @Test
    void dadoEstablecimientoConCuentaVigente_cuandoSeSuspende_entoncesNoAdmiteReservasHastaReactivarse() {
        Establecimiento establecimiento = establecimientos.establecimientoQueAdmiteReservas();
        String admin = usuarios.administrador().getUsuario().getEmail();
        EstablecimientoSuspenderReq req = new EstablecimientoSuspenderReq();
        req.setMotivo("Prueba");

        administradorSistemasService.suspenderEstablecimiento(establecimiento.getId(), req, admin);
        assertThat(establecimiento.admiteReservas(LocalDateTime.now())).isFalse();

        administradorSistemasService.reactivarEstablecimiento(establecimiento.getId(), admin);
        assertThat(establecimiento.admiteReservas(LocalDateTime.now())).isTrue();
    }

    private static RealizarReservaDTO reservaDe(Actividad actividad) {
        return new RealizarReservaDTO(actividad.getActividadesDias().get(0).getId().toString(), List.of());
    }
}
