package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.notificacion.NotificacionDTO;
import com.mza_agrotours.backend.dtos.productor.ProductorCreateReq;
import com.mza_agrotours.backend.dtos.solicitud_establecimiento.SolicitudEstablecimientoCreateReq;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.ScopeNotificacionNombre;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.PermisoRepository;
import com.mza_agrotours.backend.repositories.ProductorRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureCatalogo;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import com.mza_agrotours.backend.support.FixtureProductor;
import com.mza_agrotours.backend.support.FixtureUsuario;
import com.mza_agrotours.backend.support.Seq;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private FixtureEstablecimiento fixtureEstablecimiento;

    @Autowired
    private FixtureProductor fixtureProductor;

    @Autowired
    private ProductorRepository productorRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private ProductorService productorService;

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
    void agregarUnProductorLeAvisaEnLaBandejaDelEstablecimiento() {
        Establecimiento establecimiento = fixtureEstablecimiento.establecimientoActivo();
        Rol rol = rolProductorCon();
        rol.setEstablecimiento(establecimiento);
        rolRepository.save(rol);
        Usuario nuevo = fixtureUsuario.usuario("nuevo");

        ProductorCreateReq req = new ProductorCreateReq();
        req.setEmailUsuario(nuevo.getEmail());
        req.setRolId(rol.getId());
        productorService.crearProductor(establecimiento.getId(), req);

        assertThat(titulos(nuevo, establecimiento))
                .containsExactly(TipoNotificacionNombre.PRODUCTOR_AGREGADO.getTitulo());
    }

    @Test
    void unAvisoDeEstablecimientoLlegaSoloALosProductoresActivosConElPermiso() {
        Establecimiento establecimiento = fixtureEstablecimiento.establecimientoActivo();
        Rol rolConPermiso = rolProductorCon(PermisoCodigo.GESTIONAR_REEMBOLSOS_PENDIENTES);

        Productor conPermiso = productorCon(establecimiento, rolConPermiso, EstadoProductorNombre.ACTIVO);
        Productor sinPermiso = fixtureProductor.productorDe(establecimiento);
        Productor deLicencia = productorCon(establecimiento, rolConPermiso, EstadoProductorNombre.LICENCIA);
        Productor dadoDeBaja = productorCon(establecimiento, rolConPermiso, EstadoProductorNombre.ACTIVO);
        dadoDeBaja.setFechaHoraBaja(LocalDateTime.now());
        productorRepository.save(dadoDeBaja);
        Establecimiento otroEstablecimiento = fixtureEstablecimiento.establecimientoActivo();
        Productor deOtroEstablecimiento = productorCon(otroEstablecimiento, rolConPermiso, EstadoProductorNombre.ACTIVO);

        notificacionService.notificarProductores(establecimiento, PermisoCodigo.GESTIONAR_REEMBOLSOS_PENDIENTES,
                TipoNotificacionNombre.REEMBOLSO_PENDIENTE_PRODUCTOR, "/x", "Cosecha", "20/11/2026 09:30", "10000.00");

        assertThat(titulos(conPermiso.getUsuario(), establecimiento))
                .containsExactly(TipoNotificacionNombre.REEMBOLSO_PENDIENTE_PRODUCTOR.getTitulo());
        assertThat(titulos(sinPermiso.getUsuario(), establecimiento)).isEmpty();
        assertThat(titulos(deLicencia.getUsuario(), establecimiento)).isEmpty();
        assertThat(titulos(dadoDeBaja.getUsuario(), establecimiento)).isEmpty();
        assertThat(titulos(deOtroEstablecimiento.getUsuario(), otroEstablecimiento)).isEmpty();
    }

    @Test
    void noSeNotificaAProductoresConUnTipoDeOtroScope() {
        Establecimiento establecimiento = fixtureEstablecimiento.establecimientoActivo();

        assertThatThrownBy(() -> notificacionService.notificarProductores(establecimiento,
                PermisoCodigo.GESTIONAR_ACTIVIDAD, TipoNotificacionNombre.RESERVA_PAGADA, "/x", "Cosecha", "fecha", "1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noSeNotificaAAdministradoresConUnTipoDeOtroScope() {
        assertThatThrownBy(() -> notificacionService.notificarAdministradores(
                PermisoCodigo.GESTIONAR_SOLICITUD_ESTABLECIMIENTO,
                TipoNotificacionNombre.SOLICITUD_ESTABLECIMIENTO_CREADA, "/x", "Finca"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private List<String> titulos(Usuario usuario, Establecimiento establecimiento) {
        return notificacionService.listarNotificaciones(usuario.getEmail(), ScopeNotificacionNombre.ESTABLECIMIENTO,
                        establecimiento.getId(), Pageable.unpaged())
                .map(NotificacionDTO::getTitulo)
                .getContent();
    }

    /** Rol de productor nuevo con solo los permisos indicados. */
    private Rol rolProductorCon(PermisoCodigo... codigos) {
        Rol base = catalogo.rolProductor();
        Rol rol = new Rol();
        rol.setNombre("Rol con permisos " + Seq.next());
        rol.setDescripcion("Rol de productor para pruebas");
        rol.setEsProtegido(false);
        rol.setTipoPermiso(base.getTipoPermiso());
        rol.setPermisos(new ArrayList<>());
        for (PermisoCodigo codigo : codigos)
            rol.getPermisos().add(permisoRepository.findByCodigo(codigo).orElseThrow());
        return rolRepository.save(rol);
    }

    private Productor productorCon(Establecimiento establecimiento, Rol rol, EstadoProductorNombre estado) {
        Productor productor = fixtureProductor.productorDeEn(establecimiento, estado);
        productor.setRol(rol);
        return productorRepository.save(productor);
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

        solicitudEstablecimientoService.crearSolicitudEstablecimiento(req, solicitante.getEmail());
    }
}
