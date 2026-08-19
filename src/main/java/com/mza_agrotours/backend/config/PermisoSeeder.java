package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.repositories.PermisoRepository;
import com.mza_agrotours.backend.repositories.TipoPermisoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

@Component
@Order(4)
public class PermisoSeeder implements CommandLineRunner {

    /**
     * Datos iniciales de un permiso.
     */
    private record SeedPermiso(TipoPermisoNombre scope, String nombre, String descripcion) {}

    /**
     * Valores iniciales de cada permiso. Es privado a propósito: la descripción es editable
     * por un administrador en runtime, así que una vez creado el permiso la fuente de verdad
     * es la base de datos y no este mapa. El scope, en cambio, es estructural y se sincroniza
     * en cada arranque.
     */
    private static final Map<PermisoCodigo, SeedPermiso> SEEDS = Map.ofEntries(
            entry(PermisoCodigo.GESTIONAR_ADMIN,
                    new SeedPermiso(TipoPermisoNombre.ADMIN, "Gestionar Administradores", "Crear, modificar y eliminar administradores")),
            entry(PermisoCodigo.LEER_ADMIN,
                    new SeedPermiso(TipoPermisoNombre.ADMIN, "Lectura de Administradores","Ver administradores")),
            entry(PermisoCodigo.GESTIONAR_PRODUCTOR,
                    new SeedPermiso(TipoPermisoNombre.PRODUCTOR, "Gestionar Productores","Crear, modificar y eliminar productores")),
            entry(PermisoCodigo.LEER_PRODUCTOR,
                    new SeedPermiso(TipoPermisoNombre.PRODUCTOR, "Lectura de Productores","Ver productores")),
            entry(PermisoCodigo.LEER_ROLES_PRODUCTOR,
                    new SeedPermiso(TipoPermisoNombre.PRODUCTOR, "Lectura de roles de productores", "Ver los roles de productor del establecimiento")),
            entry(PermisoCodigo.GESTIONAR_SOLICITUD_ESTABLECIMIENTO,
                    new SeedPermiso(TipoPermisoNombre.ADMIN, "Gestionar solicitudes de establecimientos","Ver solicitudes de establecimiento")),
            entry(PermisoCodigo.LEER_SOLICITUD_ESTABLECIMIENTO,
                    new SeedPermiso(TipoPermisoNombre.ADMIN, "Lectura de solicitudes de establecimiento","Aceptar o rechazar solicitudes de establecimientos pendientes"))
    );

    private final PermisoRepository permisoRepository;
    private final TipoPermisoRepository tipoPermisoRepository;

    public PermisoSeeder(PermisoRepository permisoRepository,
                         TipoPermisoRepository tipoPermisoRepository) {
        this.permisoRepository = permisoRepository;
        this.tipoPermisoRepository = tipoPermisoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (PermisoCodigo permisoCodigo : PermisoCodigo.values()) {
            SeedPermiso seed = SEEDS.get(permisoCodigo);

            if (seed == null) {
                throw new IllegalStateException("Falta el seed de: " + permisoCodigo.name());
            }

            TipoPermiso tipoPermiso = this.tipoPermisoRepository.findByNombre(seed.scope())
                    .orElseThrow(() -> new IllegalStateException(
                            "Tipo de permiso no encontrado: " + seed.scope()
                                    + " (requerido por " + permisoCodigo.name() + ")"));

            Optional<Permiso> permisoExistente = permisoRepository.findByCodigo(permisoCodigo);

            if (permisoExistente.isPresent()) {
                // No se toca la descripción: puede haber sido editada por un administrador.
                permisoExistente.get().setTipoPermiso(tipoPermiso);
                continue;
            }

            Permiso permiso = new Permiso();
            permiso.setCodigo(permisoCodigo);
            permiso.setNombre(seed.nombre());
            permiso.setDescripcion(seed.descripcion());
            permiso.setTipoPermiso(tipoPermiso);
            permisoRepository.save(permiso);
        }
    }
}