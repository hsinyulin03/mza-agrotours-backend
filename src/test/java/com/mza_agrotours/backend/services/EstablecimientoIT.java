package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.ProductorRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureActividad;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import com.mza_agrotours.backend.support.FixtureUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class EstablecimientoIT extends AbstractIntegrationTest {
    @Autowired
    private EstablecimientoService establecimientoService;

    @Autowired
    private FixtureEstablecimiento fixtureEstablecimiento;

    @Autowired
    private FixtureActividad fixtureActividad;

    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    @Autowired
    private ProductorRepository productorRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private FixtureUsuario fixtureUsuario;


    @Test
    void dadoEstablecimientoCumpleCondEliminar_cuandoSeElimina_entoncesSeEliminaConSusInstancias() {
        Establecimiento establecimiento = fixtureEstablecimiento.establecimientoActivo();

        List<Actividad> actividades = establecimiento.getActividades();
        actividades.add(fixtureActividad.actividadBorradorEn(establecimiento));
        actividades.add(fixtureActividad.actividadBajadaEn(establecimiento));

        establecimiento = establecimientoRepository.save(establecimiento);

        this.establecimientoService.bajaEstablecimiento(establecimiento.getId());

        Establecimiento eliminado = establecimientoRepository.findById(establecimiento.getId()).orElseThrow();


        // Sobre el establecimiento
        assertThat(eliminado.getFechaHoraBaja()).isNotNull();
        assertThat(eliminado.getEstadoActual().getEstadoEstablecimiento().getNombre())
                .isEqualTo(EstadoEstablecimientoNombre.DADO_DE_BAJA);

        // Sobre sus actividades
        assertThat(eliminado.getActividades()).extracting("fechaHoraBaja")
                .isNotNull();

        // Sobre sus productores
        List<Productor> productores = productorRepository.findVigentesByEstablecimiento(establecimiento.getId());
        assertThat(productores).isEmpty();

        // Sobre sus roles
        List<Rol> roles = rolRepository.findVigentesEnScope(TipoPermisoNombre.PRODUCTOR, establecimiento.getId());
        assertThat(roles).isEmpty();
    }
}
