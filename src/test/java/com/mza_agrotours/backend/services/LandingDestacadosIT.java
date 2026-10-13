package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOListadoActividadVisitanteResponse;
import com.mza_agrotours.backend.dtos.establecimiento.DTOCatalogoEstablecimientoVisitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureActividad;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Por ahora se destacan las actividades publicadas y los establecimientos que admiten reservas.
 */
@Transactional
class LandingDestacadosIT extends AbstractIntegrationTest {

    @Autowired private ActividadService actividadService;
    @Autowired private EstablecimientoService establecimientoService;
    @Autowired private ActividadRepository actividadRepository;
    @Autowired private FixtureEstablecimiento establecimientos;
    @Autowired private FixtureActividad actividades;

    @Test
    void dadoEstablecimientoQueAdmiteReservas_entoncesSeDestacaConSuActividadPublicada() {
        Establecimiento establecimiento = establecimientos.establecimientoQueAdmiteReservas();
        Actividad publicada = conCalificacionMaxima(actividades.actividadPublicadaEn(establecimiento));
        Actividad borrador = actividades.actividadBorradorEn(establecimiento);

        assertThat(idsEstablecimientosDestacados()).contains(establecimiento.getId());
        assertThat(idsActividadesDestacadas())
                .contains(publicada.getId())
                .doesNotContain(borrador.getId());
    }

    @Test
    void dadoEstablecimientosQueNoAdmitenReservas_entoncesNoSeDestacanNiSusActividades() {
        Establecimiento sinCuenta = establecimientos.establecimientoActivo();
        Establecimiento tokenVencido = establecimientos.conCuentaMercadoPago(
                establecimientos.establecimientoActivo(), LocalDateTime.now().minusDays(1));
        Establecimiento suspendido = establecimientos.conCuentaMercadoPago(
                establecimientos.establecimientoEn(EstadoEstablecimientoNombre.SUSPENDIDO),
                LocalDateTime.now().plusDays(150));

        List<UUID> actividadesExcluidas = List.of(
                conCalificacionMaxima(actividades.actividadPublicadaEn(sinCuenta)).getId(),
                conCalificacionMaxima(actividades.actividadPublicadaEn(tokenVencido)).getId(),
                conCalificacionMaxima(actividades.actividadPublicadaEn(suspendido)).getId());

        assertThat(idsEstablecimientosDestacados())
                .doesNotContain(sinCuenta.getId(), tokenVencido.getId(), suspendido.getId());
        assertThat(idsActividadesDestacadas()).doesNotContainAnyElementsOf(actividadesExcluidas);
    }

    @Test
    void dadoMasDeSeisCandidatos_entoncesSeDevuelvenSeis() {
        for (int i = 0; i < 7; i++) {
            actividades.actividadPublicadaEn(establecimientos.establecimientoQueAdmiteReservas());
        }

        assertThat(establecimientoService.obtenerEstablecimientosDestacados()).hasSize(6);
        assertThat(actividadService.obtenerActividadesDestacadas()).hasSize(6);
    }

    private Actividad conCalificacionMaxima(Actividad actividad) {
        // Las destacadas se ordenan por calificación: así no quedan afuera por otros datos de la base
        actividad.setCalificacionPromedio(5f);
        return actividadRepository.save(actividad);
    }

    private List<UUID> idsEstablecimientosDestacados() {
        return establecimientoService.obtenerEstablecimientosDestacados().stream()
                .map(DTOCatalogoEstablecimientoVisitante::getId)
                .toList();
    }

    private List<UUID> idsActividadesDestacadas() {
        return actividadService.obtenerActividadesDestacadas().stream()
                .map(DTOListadoActividadVisitanteResponse::getId)
                .toList();
    }
}
