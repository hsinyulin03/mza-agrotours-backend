package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.administrador_sistemas.EstablecimientoSuspenderReq;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.establecimiento.EstablecimientoEstado;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import com.mza_agrotours.backend.support.FixtureUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class AdministradorSistemasServiceIT extends AbstractIntegrationTest {

    @Autowired
    private AdministradorSistemasService administradorSistemasService;

    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    @Autowired
    private FixtureEstablecimiento establecimientos;

    @Autowired
    private FixtureUsuario usuarios;

    @Test
    void dadoEstablecimiento_cuandoSuspende_entoncesCierraElTramoVigenteYAbreUnoSuspendido() {
        Establecimiento establecimiento = establecimientos.establecimientoActivo();
        AdministradorSistemas ejecutor = usuarios.administrador();

        EstablecimientoSuspenderReq req = new EstablecimientoSuspenderReq();
        req.setMotivo("Incumplimiento de normativa");

        administradorSistemasService.suspenderEstablecimiento(
                establecimiento.getId(), req, ejecutor.getUsuario().getEmail());

        Establecimiento actualizado = this.establecimientoRepository
                .findById(establecimiento.getId())
                .orElseThrow();

        assertThat(actualizado.getEstadoActual().getEstadoEstablecimiento().getNombre())
                .isEqualTo(EstadoEstablecimientoNombre.SUSPENDIDO);
        assertThat(actualizado.getEstadoActual().getMotivo())
                .isEqualTo("Incumplimiento de normativa");
        assertThat(actualizado.getEstadoActual().getEjecutor().getId())
                .isEqualTo(ejecutor.getId());

        assertThat(actualizado.getEstados())
                .hasSize(2)
                .filteredOn(tramo -> tramo.getFechaFin() == null)
                .extracting(EstablecimientoEstado::getEstadoEstablecimiento)
                .extracting("nombre")
                .containsExactly(EstadoEstablecimientoNombre.SUSPENDIDO);
    }
}
