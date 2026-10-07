package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.notificacion.NotificacionDTO;
import com.mza_agrotours.backend.dtos.solicitud_establecimiento.SolicitudEstablecimientoCreateReq;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.ScopeNotificacionNombre;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureCatalogo;
import com.mza_agrotours.backend.support.FixtureUsuario;
import com.mza_agrotours.backend.support.Seq;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class NotificacionScopeIT extends AbstractIntegrationTest {

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private SolicitudEstablecimientoService solicitudEstablecimientoService;

    @Autowired
    private AdministradorSistemasRepository administradorSistemasRepository;

    @Autowired
    private FixtureUsuario fixtureUsuario;

    @Autowired
    private FixtureCatalogo catalogo;

    @Test
    void unaSolicitudNuevaLlegaALaBandejaDeCadaAdministradorVigente() {
        AdministradorSistemas admin = fixtureUsuario.administrador();
        AdministradorSistemas otroAdmin = fixtureUsuario.administrador();
        AdministradorSistemas adminDadoDeBaja = fixtureUsuario.administrador();
        adminDadoDeBaja.setFechaHoraBaja(LocalDateTime.now());
        administradorSistemasRepository.save(adminDadoDeBaja);

        crearSolicitud(fixtureUsuario.usuario("solicitante"));

        assertThat(titulos(admin.getUsuario(), ScopeNotificacionNombre.ADMINISTRADOR))
                .containsExactly(TipoNotificacionNombre.SOLICITUD_ESTABLECIMIENTO_POR_REVISAR.getTitulo());
        assertThat(titulos(otroAdmin.getUsuario(), ScopeNotificacionNombre.ADMINISTRADOR))
                .containsExactly(TipoNotificacionNombre.SOLICITUD_ESTABLECIMIENTO_POR_REVISAR.getTitulo());
        assertThat(titulos(adminDadoDeBaja.getUsuario(), ScopeNotificacionNombre.ADMINISTRADOR)).isEmpty();
    }

    /**
     * Visitante y administrador no tienen establecimiento: antes del scope las dos
     * bandejas eran la misma consulta y se mezclaban.
     */
    @Test
    void lasBandejasDeVisitanteYAdministradorNoSeMezclan() {
        AdministradorSistemas admin = fixtureUsuario.administrador();

        crearSolicitud(admin.getUsuario());

        assertThat(titulos(admin.getUsuario(), ScopeNotificacionNombre.VISITANTE))
                .containsExactly(TipoNotificacionNombre.SOLICITUD_ESTABLECIMIENTO_CREADA.getTitulo());
        assertThat(titulos(admin.getUsuario(), ScopeNotificacionNombre.ADMINISTRADOR))
                .containsExactly(TipoNotificacionNombre.SOLICITUD_ESTABLECIMIENTO_POR_REVISAR.getTitulo());
        assertThat(notificacionService.contarNoLeidas(
                admin.getUsuario().getEmail(), ScopeNotificacionNombre.VISITANTE, null)).isEqualTo(1);
    }

    @Test
    void unTipoDeEstablecimientoNoSeCreaSinEstablecimiento() {
        Usuario usuario = fixtureUsuario.usuario("productor");

        assertThatThrownBy(() -> notificacionService.crearNotificacion(
                usuario, TipoNotificacionNombre.PRODUCTOR_AGREGADO, null, "/x", "Finca"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requiere establecimiento");
    }

    @Test
    void noSeNotificaAAdministradoresConUnTipoDeOtroScope() {
        assertThatThrownBy(() -> notificacionService.notificarAdministradores(
                PermisoCodigo.GESTIONAR_SOLICITUD_ESTABLECIMIENTO,
                TipoNotificacionNombre.SOLICITUD_ESTABLECIMIENTO_CREADA, "/x", "Finca"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private List<String> titulos(Usuario usuario, ScopeNotificacionNombre scope) {
        return notificacionService.listarNotificaciones(usuario.getEmail(), scope, null, Pageable.unpaged())
                .map(NotificacionDTO::getTitulo)
                .getContent();
    }

    private void crearSolicitud(Usuario solicitante) {
        int n = Seq.next();

        SolicitudEstablecimientoCreateReq req = new SolicitudEstablecimientoCreateReq();
        req.setNombreEstablecimiento("Finca " + n);
        req.setRazonSocial("Finca " + n + " S.A.");
        req.setCuit(String.format("20%09d", n));
        req.setDescripcion("Solicitud de prueba " + n);
        req.setDomicilioLegal("Calle Falsa " + n);
        req.setDepartamento(catalogo.unDepartamento().getNombre());
        req.setTelefono("2610000000");
        req.setEmail("finca" + n + "@test.local");
        req.setCvu(String.format("%022d", n));

        solicitudEstablecimientoService.crearSolicitudEstablecimiento(req, solicitante.getEmail());
    }
}
