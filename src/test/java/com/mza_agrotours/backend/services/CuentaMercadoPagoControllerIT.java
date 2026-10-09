package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.controllers.CuentaMercadoPagoController;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import com.mza_agrotours.backend.support.FixtureProductor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Solo el productor líder (titular) puede ver el estado de la cuenta de MP del establecimiento y pedir la URL
 * de vinculación. Se llama al controller por su proxy de Spring, así se evalúan los @PreAuthorize reales.
 */
@Transactional
class CuentaMercadoPagoControllerIT extends AbstractIntegrationTest {

    @Autowired private CuentaMercadoPagoController controller;
    @Autowired private FixtureEstablecimiento establecimientos;
    @Autowired private FixtureProductor productores;

    private Establecimiento establecimiento;

    @BeforeEach
    void setUp() {
        establecimiento = establecimientos.establecimientoActivo();
    }

    @AfterEach
    void limpiarSesion() {
        SecurityContextHolder.clearContext();
    }

    /** Lo que arma FirebaseTokenFilter tras validar el token: el predicado solo mira el email del principal. */
    private static void autenticadoComo(Productor productor) {
        UsuarioAuthDetails principal = UsuarioAuthDetails.builder()
                .email(productor.getUsuario().getEmail())
                .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @Test
    void elTitularVeElEstadoYObtieneLaUrlDeVinculacion() {
        autenticadoComo(establecimiento.getTitular());

        assertThat(controller.obtenerEstado(establecimiento.getId()).getBody().getData().vinculada()).isFalse();
        assertThat(controller.obtenerUrlVinculacion(establecimiento.getId()).getBody().getData())
                .startsWith("https://auth.mercadopago.com.ar/authorization?")
                .contains("state=");
    }

    @Test
    void unProductorQueNoEsTitularNoPuedeVerNiVincular() {
        autenticadoComo(productores.productorDe(establecimiento));

        assertThatThrownBy(() -> controller.obtenerEstado(establecimiento.getId()))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> controller.obtenerUrlVinculacion(establecimiento.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void elTitularDeOtroEstablecimientoNoPuedeVincularEste() {
        autenticadoComo(establecimientos.establecimientoActivo().getTitular());

        assertThatThrownBy(() -> controller.obtenerUrlVinculacion(establecimiento.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void unUsuarioAnonimoNoPuedeVincular() {
        // Un request sin token de Firebase llega con la autenticación anónima de Spring Security
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "anonimo", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertThatThrownBy(() -> controller.obtenerUrlVinculacion(establecimiento.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }
}
