package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;
import com.mza_agrotours.backend.repositories.ProductorRepository;
import com.mza_agrotours.backend.security.EstablecimientoAuthorization;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import com.mza_agrotours.backend.support.FixtureProductor;
import com.mza_agrotours.backend.support.FixtureUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Suspender un productor tiene que cortarle el acceso, y quien decide eso no es el service sino
 * el predicado que evaluan los @PreAuthorize. Estos tests lo interrogan directamente contra la
 * base real: si el predicado sigue dando true despues de la suspension, todos los endpoints que
 * lo usan quedan abiertos.
 */
@Transactional
class ProductorSuspensionIT extends AbstractIntegrationTest {

    private static final String MOTIVO = "Incumplimiento de normativa";

    @Autowired
    private ProductorService productorService;

    @Autowired
    private EstablecimientoAuthorization estAuth;

    @Autowired
    private ProductorRepository productorRepository;

    @Autowired
    private FixtureEstablecimiento establecimientos;

    @Autowired
    private FixtureProductor productores;

    @Autowired
    private FixtureUsuario usuarios;

    private Productor productor;
    private AdministradorSistemas ejecutor;
    private Establecimiento establecimiento;

    @BeforeEach
    void setUp() {
        this.establecimiento = establecimientos.establecimientoActivo();
        this.productor = productores.productorDe(establecimiento);
        this.ejecutor = usuarios.administrador();
    }

    @Test
    void dadoProductorActivo_cuandoSeSuspende_entoncesDejaDePasarEsProductorVigente() {
        Authentication autenticacion = autenticadoComo(productor);

        assertThat(estAuth.esProductorVigente(autenticacion, establecimiento.getId()))
                .as("un productor activo pasa el predicado antes de la suspension")
                .isTrue();

        suspenderProductor();

        assertThat(estAuth.esProductorVigente(autenticacion, establecimiento.getId()))
                .as("suspendido deja de pasarlo")
                .isFalse();
    }

    @Test
    void dadoProductorSuspendido_cuandoSeLevantaLaSuspension_entoncesVuelveAPasarElPredicado() {
        Authentication autenticacion = autenticadoComo(productor);
        suspenderProductor();

        productorService.levantarSuspension(establecimiento.getId(), productor.getId(),
                "Fin de la sancion", ejecutor.getUsuario().getEmail());

        assertThat(estAuth.esProductorVigente(autenticacion, establecimiento.getId())).isTrue();
    }

    @Test
    void dadoProductorSuspendido_cuandoOtroProductorConsulta_entoncesSuAccesoNoSeVeAfectado() {
        Productor otro = productores.productorDe(establecimiento);
        suspenderProductor();

        assertThat(estAuth.esProductorVigente(autenticadoComo(otro), establecimiento.getId())).isTrue();
    }

    @Test
    void dadoProductorSuspendido_entoncesElTramoVigenteQuedaEnLicenciaConVencimiento() {
        LocalDateTime vencimiento = LocalDateTime.now().plusDays(10);

        productorService.suspenderProductor(establecimiento.getId(), productor.getId(),
                MOTIVO, vencimiento, ejecutor.getUsuario().getEmail());

        Productor actualizado = productorRepository.findById(productor.getId()).orElseThrow();

        assertThat(actualizado.getEstadoActual().getNombre()).isEqualTo(EstadoProductorNombre.LICENCIA);
        assertThat(actualizado.getEstados())
                .filteredOn(tramo -> tramo.getFechaHoraFin() == null)
                .singleElement()
                .satisfies(tramo -> {
                    assertThat(tramo.getMotivo()).isEqualTo(MOTIVO);
                    assertThat(tramo.getFechaHoraFinPrevista()).isEqualTo(vencimiento);
                });
    }

    private void suspenderProductor() {
        productorService.suspenderProductor(establecimiento.getId(), productor.getId(),
                MOTIVO, LocalDateTime.now().plusDays(10), ejecutor.getUsuario().getEmail());
    }

    /**
     * Lo que arma FirebaseTokenFilter tras validar el token: el predicado solo mira el email
     * del principal, no las authorities.
     */
    private Authentication autenticadoComo(Productor productor) {
        UsuarioAuthDetails principal = UsuarioAuthDetails.builder()
                .email(productor.getUsuario().getEmail())
                .build();
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }
}
