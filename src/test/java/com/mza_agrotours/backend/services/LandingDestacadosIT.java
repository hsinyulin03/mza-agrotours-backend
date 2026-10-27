package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.DTOListadoActividadVisitanteResponse;
import com.mza_agrotours.backend.dtos.establecimiento.DTOCatalogoEstablecimientoVisitante;
import com.mza_agrotours.backend.entities.Calificacion;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.repositories.CalificacionRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureActividad;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
    @Autowired private CalificacionRepository calificacionRepository;
    @Autowired private EstablecimientoRepository establecimientoRepository;
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

    @Test
    void dadoPromedioBayesiano_entoncesLaCantidadDeReseniasPesaComoConfianzaYNoComoVolumen() {
        Actividad unaDeCinco = conResenias(actividadQueAdmiteReservas(), 1, 5);
        Actividad diezDeCuatroYMedio = conResenias(conResenias(actividadQueAdmiteReservas(), 5, 5), 5, 4);
        Actividad veinteDeUno = conResenias(actividadQueAdmiteReservas(), 20, 1);
        Actividad diezDeCinco = conResenias(actividadQueAdmiteReservas(), 10, 5);
        Actividad cincuentaYUnaDeUno = conResenias(actividadQueAdmiteReservas(), 51, 1);

        List<UUID> orden = actividadRepository
                .obtenerActividadesDestacadas(LocalDateTime.now(), 3.5, 5, PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .map(Actividad::getId)
                .filter(List.of(unaDeCinco.getId(), diezDeCuatroYMedio.getId(), veinteDeUno.getId(),
                        diezDeCinco.getId(), cincuentaYUnaDeUno.getId())::contains)
                .toList();

        assertThat(orden).containsExactly(diezDeCinco.getId(), diezDeCuatroYMedio.getId(), unaDeCinco.getId(),
                veinteDeUno.getId(), cincuentaYUnaDeUno.getId());
    }

    @Test
    void dadoPromedioBayesiano_entoncesLosEstablecimientosSumanLasReseniasDeTodasSusActividades() {
        Establecimiento unaDeCinco = establecimientos.establecimientoQueAdmiteReservas();
        conResenias(actividades.actividadPublicadaEn(unaDeCinco), 1, 5);
        Establecimiento diezDeCincoEnDosActividades = establecimientos.establecimientoQueAdmiteReservas();
        conResenias(actividades.actividadPublicadaEn(diezDeCincoEnDosActividades), 5, 5);
        conResenias(actividades.actividadPublicadaEn(diezDeCincoEnDosActividades), 5, 5);
        Establecimiento veinteDeUno = establecimientos.establecimientoQueAdmiteReservas();
        conResenias(actividades.actividadPublicadaEn(veinteDeUno), 20, 1);

        List<UUID> orden = establecimientoRepository
                .obtenerEstablecimientosDestacados(LocalDateTime.now(), 3.5, 5, PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .map(Establecimiento::getId)
                .filter(List.of(unaDeCinco.getId(), diezDeCincoEnDosActividades.getId(), veinteDeUno.getId())::contains)
                .toList();

        assertThat(orden).containsExactly(diezDeCincoEnDosActividades.getId(), unaDeCinco.getId(), veinteDeUno.getId());
    }

    private Actividad actividadQueAdmiteReservas() {
        return actividades.actividadPublicadaEn(establecimientos.establecimientoQueAdmiteReservas());
    }

    private Actividad conCalificacionMaxima(Actividad actividad) {
        // Las destacadas se ordenan por calificación: así no quedan afuera por otros datos de la base
        return conResenias(actividad, 50, 5);
    }

    private Actividad conResenias(Actividad actividad, int cantidad, int puntaje) {
        for (int i = 0; i < cantidad; i++) {
            Calificacion calificacion = new Calificacion(puntaje, null, LocalDateTime.now());
            actividad.getCalificaciones().add(calificacion);
            calificacionRepository.save(calificacion);
        }
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
