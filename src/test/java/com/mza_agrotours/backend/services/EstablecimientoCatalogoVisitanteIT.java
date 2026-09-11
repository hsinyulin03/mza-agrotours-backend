package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.TipoCultivo.TipoCultivoRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureActividad;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class EstablecimientoCatalogoVisitanteIT extends AbstractIntegrationTest {

    private static final Pageable PRIMERA_PAGINA = PageRequest.of(0, 200);

    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;

    @Autowired
    private FixtureEstablecimiento fixtureEstablecimiento;

    @Autowired
    private FixtureActividad fixtureActividad;

    @Test
    void dadoEstablecimientosSinActividadesPublicadas_cuandoSeConsultaSinFiltros_entoncesAparecenEnElCatalogo() {
        Establecimiento sinActividades = fixtureEstablecimiento.establecimientoActivo();
        Establecimiento soloBorrador = fixtureEstablecimiento.establecimientoActivo();
        fixtureActividad.actividadBorradorEn(soloBorrador);

        Page<Establecimiento> pagina = establecimientoRepository
                .obtenerEstablecimientosActivos(null, null, PRIMERA_PAGINA);

        assertThat(idsDe(pagina)).contains(sinActividades.getId(), soloBorrador.getId());
    }

    @Test
    void dadoFiltroPorCultivo_cuandoSeConsulta_entoncesSoloApareceQuienLoOfreceEnActividadPublicada() {
        TipoCultivo vid = cultivo();

        Establecimiento conVidPublicada = fixtureEstablecimiento.establecimientoActivo();
        agregarCultivo(fixtureActividad.actividadPublicadaEn(conVidPublicada), vid);

        Establecimiento conVidEnBorrador = fixtureEstablecimiento.establecimientoActivo();
        agregarCultivo(fixtureActividad.actividadBorradorEn(conVidEnBorrador), vid);

        Establecimiento sinActividades = fixtureEstablecimiento.establecimientoActivo();

        Page<Establecimiento> pagina = establecimientoRepository
                .obtenerEstablecimientosActivos(List.of(vid.getId()), null, PRIMERA_PAGINA);

        assertThat(idsDe(pagina))
                .contains(conVidPublicada.getId())
                .doesNotContain(conVidEnBorrador.getId(), sinActividades.getId());
    }

    @Test
    void dadoEstablecimientoConVariasActividadesDelMismoCultivo_cuandoSeConsulta_entoncesNoSeDuplicaNiDesajustaElTotal() {
        TipoCultivo vid = cultivo();

        Establecimiento establecimiento = fixtureEstablecimiento.establecimientoActivo();
        agregarCultivo(fixtureActividad.actividadPublicadaEn(establecimiento), vid);
        agregarCultivo(fixtureActividad.actividadPublicadaEn(establecimiento), vid);

        Page<Establecimiento> pagina = establecimientoRepository
                .obtenerEstablecimientosActivos(List.of(vid.getId()), null, PRIMERA_PAGINA);

        assertThat(idsDe(pagina)).containsOnlyOnce(establecimiento.getId());
        assertThat(pagina.getTotalElements()).isEqualTo(pagina.getContent().size());
    }

    @Test
    void dadoEstablecimientoSuspendido_cuandoSeConsultaSinFiltros_entoncesNoAparece() {
        Establecimiento suspendido = fixtureEstablecimiento
                .establecimientoEn(EstadoEstablecimientoNombre.SUSPENDIDO);
        fixtureActividad.actividadPublicadaEn(suspendido);

        Page<Establecimiento> pagina = establecimientoRepository
                .obtenerEstablecimientosActivos(null, null, PRIMERA_PAGINA);

        assertThat(idsDe(pagina)).doesNotContain(suspendido.getId());
    }

    private List<UUID> idsDe(Page<Establecimiento> pagina) {
        return pagina.getContent().stream().map(Establecimiento::getId).toList();
    }

    private TipoCultivo cultivo() {
        String n = UUID.randomUUID().toString();

        TipoCultivo tipoCultivo = new TipoCultivo();
        tipoCultivo.setNombre("Cultivo " + n);
        tipoCultivo.setDescripcion("Cultivo de prueba " + n);
        tipoCultivo.setPorcionReferencia("100 g");
        return tipoCultivoRepository.save(tipoCultivo);
    }

    private void agregarCultivo(Actividad actividad, TipoCultivo tipoCultivo) {
        actividad.getCultivos().add(tipoCultivo);
        actividadRepository.save(actividad);
    }
}
