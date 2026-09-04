package com.mza_agrotours.backend.support;

import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.Pais;
import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.TipoIdentificacionNombre;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.actividad.ActividadDiaEstado;
import com.mza_agrotours.backend.entities.actividad.EstadoActividad;
import com.mza_agrotours.backend.entities.actividad.EstadoActividadDia;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.establecimiento.EstadoEstablecimiento;
import com.mza_agrotours.backend.entities.productor.EstadoProductor;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.repositories.*;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadDiaRepository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Persisted test data for integration tests.
 *
 * <p>Every entity is built valid against the entity model constraints and reuses the catalog
 * rows the seeders create, so a test only has to state the part it cares about. Values under a
 * uniqueness constraint carry a sequence suffix, so several fixtures can coexist in one test.
 */
@Component
public class Fixtures {

    private static final String ROL_ADMIN_SEMILLA = "Admin prueba";
    private static final String ROL_PRODUCTOR_PRUEBA = "Productor de prueba";
    private static final AtomicInteger SEQ = new AtomicInteger();

    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private PaisRepository paisRepository;
    @Autowired private TipoIdentificacionRepository tipoIdentificacionRepository;
    @Autowired private TipoPermisoRepository tipoPermisoRepository;
    @Autowired private EstadoEstablecimientoRepository estadoEstablecimientoRepository;
    @Autowired private EstadoProductorRepository estadoProductorRepository;
    @Autowired private EstadoActividadRepository estadoActividadRepository;
    @Autowired private EstadoActividadDiaRepository estadoActividadDiaRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private VisitanteRepository visitanteRepository;
    @Autowired private EstablecimientoRepository establecimientoRepository;
    @Autowired private ProductorRepository productorRepository;
    @Autowired private ActividadRepository actividadRepository;
    @Autowired private AdministradorSistemasRepository administradorSistemasRepository;

    public Establecimiento establecimientoActivo() {
        return establecimientoEn(EstadoEstablecimientoNombre.ACTIVO);
    }

    public Establecimiento establecimientoEn(EstadoEstablecimientoNombre estadoNombre) {
        int n = SEQ.incrementAndGet();

        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setNombre("Finca " + n);
        establecimiento.setRazonSocial("Finca " + n + " S.A.");
        establecimiento.setCuit(String.format("20%09d", n));
        establecimiento.setFechaHoraAlta(LocalDateTime.now());
        establecimiento.setDescripcion("Establecimiento de prueba " + n);
        establecimiento.setTelefono("2610000000");
        establecimiento.setEmail("finca" + n + "@test.local");
        establecimiento.setUbicacion("-32.8895,-68.8458");
        establecimiento.setCvu(String.format("%022d", n));
        establecimiento.setDepartamento(unDepartamento());
        establecimiento = this.establecimientoRepository.save(establecimiento);

        // Productor.establecimiento es optional=false y Establecimiento.titular apunta de vuelta:
        // el ciclo solo se puede cerrar en una segunda escritura.
        establecimiento.setTitular(titularDe(establecimiento, n));
        establecimiento.cambiarEstado(estadoEstablecimiento(estadoNombre), "Alta de prueba");
        return this.establecimientoRepository.save(establecimiento);
    }

    public AdministradorSistemas administrador() {
        int n = SEQ.incrementAndGet();

        AdministradorSistemas administrador = new AdministradorSistemas();
        administrador.setUsuario(usuario("admin" + n + "@test.local", n));
        administrador.setRol(rolAdmin());
        administrador.setFechaHoraAlta(LocalDateTime.now());
        return this.administradorSistemasRepository.save(administrador);
    }

    public Visitante visitante() {
        int n = SEQ.incrementAndGet();

        Visitante visitante = new Visitante();
        visitante.setUsuario(usuario("visitante" + n + "@test.local", n));
        visitante.setPais(unPais());
        return this.visitanteRepository.save(visitante);
    }

    public Actividad actividadPublicadaEn(Establecimiento establecimiento) {
        int n = SEQ.incrementAndGet();

        Actividad actividad = new Actividad();
        actividad.setNombre("Actividad " + n);
        actividad.setDescripcion("Actividad de prueba numero " + n);
        actividad.setCuposMax(20);
        actividad.setEstado(estadoActividad(EstadoActividadNombre.PUBLICADO));
        actividad.setEstablecimiento(establecimiento);
        return this.actividadRepository.save(actividad);
    }

    public Actividad actividadConDiaReservableEn(Establecimiento establecimiento) {
        Actividad actividad = actividadPublicadaEn(establecimiento);

        ActividadDiaEstado estado = new ActividadDiaEstado();
        estado.setFechaHoraInicio(LocalDateTime.now().minusDays(1));
        estado.setEstado(estadoActividadDia(EstadoActividadDiaNombre.ACTIVA));

        ActividadDia dia = new ActividadDia();
        // handleIniciarReserva solo mira dias cuya fechaHoraInicio ya paso.
        dia.setFechaHoraInicio(LocalDateTime.now().minusHours(1));
        dia.setFechaHoraFin(LocalDateTime.now().plusHours(3));
        dia.setCuposMax(20);
        dia.registrarNuevoEstado(estado);

        actividad.addActividadDia(dia);
        return this.actividadRepository.save(actividad);
    }

    private Productor titularDe(Establecimiento establecimiento, int n) {
        Productor titular = new Productor();
        titular.setFechaHoraAlta(LocalDateTime.now());
        titular.setEstablecimiento(establecimiento);
        titular.setUsuario(usuario("productor" + n + "@test.local", n));
        titular.setRol(rolProductor());
        titular.cambiarEstado(estadoProductor(EstadoProductorNombre.ACTIVO),
                "Alta de prueba", LocalDateTime.now(), null);
        return this.productorRepository.save(titular);
    }

    private Usuario usuario(String email, int n) {
        Usuario usuario = new Usuario();
        usuario.setFirebaseUID("uid-" + email);
        usuario.setEmail(email);
        usuario.setNombre("Usuario " + n);
        usuario.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        usuario.setTelefono("2610000000");
        usuario.setFechaHoraAlta(LocalDateTime.now());
        usuario.setIdentificacion(String.format("%08d", n));
        usuario.setTipoIdentificacion(tipoIdentificacion());
        return this.usuarioRepository.save(usuario);
    }

    private Rol rolAdmin() {
        return this.rolRepository.findByNombre(ROL_ADMIN_SEMILLA)
                .orElseThrow(() -> new IllegalStateException(
                        "RolSeeder no creo el rol " + ROL_ADMIN_SEMILLA));
    }

    private Rol rolProductor() {
        return this.rolRepository.findByNombre(ROL_PRODUCTOR_PRUEBA)
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre(ROL_PRODUCTOR_PRUEBA);
                    rol.setDescripcion("Rol de productor para pruebas");
                    rol.setEsProtegido(false);
                    rol.setTipoPermiso(tipoPermiso(TipoPermisoNombre.PRODUCTOR));
                    rol.setPermisos(new ArrayList<>());
                    return this.rolRepository.save(rol);
                });
    }

    private TipoPermiso tipoPermiso(TipoPermisoNombre nombre) {
        return this.tipoPermisoRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("TipoPermisoSeeder no creo " + nombre));
    }

    private EstadoEstablecimiento estadoEstablecimiento(EstadoEstablecimientoNombre nombre) {
        return this.estadoEstablecimientoRepository.findByNombreAndFechaBajaIsNull(nombre)
                .orElseThrow(() -> new IllegalStateException(
                        "EstadoEstablecimientoSeeder no creo " + nombre));
    }

    private EstadoProductor estadoProductor(EstadoProductorNombre nombre) {
        return this.estadoProductorRepository.findByNombreAndFechaHoraBajaIsNull(nombre)
                .orElseThrow(() -> new IllegalStateException("EstadoProductorSeeder no creo " + nombre));
    }

    private EstadoActividad estadoActividad(EstadoActividadNombre nombre) {
        return this.estadoActividadRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("EstadoActividadSeeder no creo " + nombre));
    }

    private EstadoActividadDia estadoActividadDia(EstadoActividadDiaNombre nombre) {
        return this.estadoActividadDiaRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("EstadoActividadDiaSeeder no creo " + nombre));
    }

    private TipoIdentificacion tipoIdentificacion() {
        return this.tipoIdentificacionRepository.findByNombre(TipoIdentificacionNombre.DNI)
                .orElseThrow(() -> new IllegalStateException("TipoIdentificacionSeeder no creo DNI"));
    }

    private Departamento unDepartamento() {
        return this.departamentoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("DepartamentoSeeder no cargo departamentos"));
    }

    private Pais unPais() {
        return this.paisRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("PaisSeeder no cargo paises"));
    }
}
