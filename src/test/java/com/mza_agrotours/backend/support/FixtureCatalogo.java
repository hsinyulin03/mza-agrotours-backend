package com.mza_agrotours.backend.support;

import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.Pais;
import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.TipoIdentificacionNombre;
import com.mza_agrotours.backend.entities.actividad.EstadoActividad;
import com.mza_agrotours.backend.entities.actividad.EstadoActividadDia;
import com.mza_agrotours.backend.entities.establecimiento.EstadoEstablecimiento;
import com.mza_agrotours.backend.entities.productor.EstadoProductor;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.repositories.DepartamentoRepository;
import com.mza_agrotours.backend.repositories.EstadoEstablecimientoRepository;
import com.mza_agrotours.backend.repositories.EstadoProductorRepository;
import com.mza_agrotours.backend.repositories.PaisRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
import com.mza_agrotours.backend.repositories.TipoIdentificacionRepository;
import com.mza_agrotours.backend.repositories.TipoPermisoRepository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadDiaRepository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Filas de catalogo que los seeders ya dejaron cargadas. Se resuelven aca, y no en cada fixture,
 * para que el error sea siempre el mismo cuando falta una semilla.
 */
@Component
public class FixtureCatalogo {

    private static final String ROL_ADMIN_SEMILLA = "Admin prueba";
    private static final String ROL_PRODUCTOR_PRUEBA = "Productor de prueba";

    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private PaisRepository paisRepository;
    @Autowired private TipoIdentificacionRepository tipoIdentificacionRepository;
    @Autowired private TipoPermisoRepository tipoPermisoRepository;
    @Autowired private EstadoEstablecimientoRepository estadoEstablecimientoRepository;
    @Autowired private EstadoProductorRepository estadoProductorRepository;
    @Autowired private EstadoActividadRepository estadoActividadRepository;
    @Autowired private EstadoActividadDiaRepository estadoActividadDiaRepository;
    @Autowired private RolRepository rolRepository;

    public Rol rolAdmin() {
        return this.rolRepository.findByNombre(ROL_ADMIN_SEMILLA)
                .orElseThrow(() -> new IllegalStateException(
                        "RolSeeder no creo el rol " + ROL_ADMIN_SEMILLA));
    }

    public Rol rolProductor() {
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

    public TipoPermiso tipoPermiso(TipoPermisoNombre nombre) {
        return this.tipoPermisoRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("TipoPermisoSeeder no creo " + nombre));
    }

    public EstadoEstablecimiento estadoEstablecimiento(EstadoEstablecimientoNombre nombre) {
        return this.estadoEstablecimientoRepository.findByNombreAndFechaBajaIsNull(nombre)
                .orElseThrow(() -> new IllegalStateException(
                        "EstadoEstablecimientoSeeder no creo " + nombre));
    }

    public EstadoProductor estadoProductor(EstadoProductorNombre nombre) {
        return this.estadoProductorRepository.findByNombreAndFechaHoraBajaIsNull(nombre)
                .orElseThrow(() -> new IllegalStateException("EstadoProductorSeeder no creo " + nombre));
    }

    public EstadoActividad estadoActividad(EstadoActividadNombre nombre) {
        return this.estadoActividadRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("EstadoActividadSeeder no creo " + nombre));
    }

    public EstadoActividadDia estadoActividadDia(EstadoActividadDiaNombre nombre) {
        return this.estadoActividadDiaRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("EstadoActividadDiaSeeder no creo " + nombre));
    }

    public TipoIdentificacion tipoIdentificacion() {
        return this.tipoIdentificacionRepository.findByNombre(TipoIdentificacionNombre.DNI)
                .orElseThrow(() -> new IllegalStateException("TipoIdentificacionSeeder no creo DNI"));
    }

    public Departamento unDepartamento() {
        return this.departamentoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("DepartamentoSeeder no cargo departamentos"));
    }

    public Pais unPais() {
        return this.paisRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("PaisSeeder no cargo paises"));
    }
}
