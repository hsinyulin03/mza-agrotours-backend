package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.PermisoNombre;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermisoNombre;
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
    private record SeedPermiso(TipoPermisoNombre scope, String descripcion) {}

    /**
     * Valores iniciales de cada permiso. Es privado a propósito: la descripción es editable
     * por un administrador en runtime, así que una vez creado el permiso la fuente de verdad
     * es la base de datos y no este mapa. El scope, en cambio, es estructural y se sincroniza
     * en cada arranque.
     */
    private static final Map<PermisoNombre, SeedPermiso> SEEDS = Map.ofEntries(
            entry(PermisoNombre.CREAR_ADMIN,
                    new SeedPermiso(TipoPermisoNombre.ADMIN, "Crear administradores")),
            entry(PermisoNombre.MODIFICAR_ADMIN,
                    new SeedPermiso(TipoPermisoNombre.ADMIN, "Modificar administradores")),
            entry(PermisoNombre.ELIMINAR_ADMIN,
                    new SeedPermiso(TipoPermisoNombre.ADMIN, "Eliminar administradores")),
            entry(PermisoNombre.CREAR_PRODUCTOR,
                    new SeedPermiso(TipoPermisoNombre.PRODUCTOR, "Crear productores")),
            entry(PermisoNombre.MODIFICAR_PRODUCTOR,
                    new SeedPermiso(TipoPermisoNombre.PRODUCTOR, "Modificar productores")),
            entry(PermisoNombre.ELIMINAR_PRODUCTOR,
                    new SeedPermiso(TipoPermisoNombre.PRODUCTOR, "Eliminar productores"))
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
        for (PermisoNombre permisoNombre : PermisoNombre.values()) {
            SeedPermiso seed = SEEDS.get(permisoNombre);

            if (seed == null) {
                throw new IllegalStateException("Falta el seed de: " + permisoNombre.name());
            }

            TipoPermiso tipoPermiso = this.tipoPermisoRepository.findByNombre(seed.scope())
                    .orElseThrow(() -> new IllegalStateException(
                            "Tipo de permiso no encontrado: " + seed.scope()
                                    + " (requerido por " + permisoNombre.name() + ")"));

            Optional<Permiso> permisoExistente = permisoRepository.findByNombre(permisoNombre);

            if (permisoExistente.isPresent()) {
                // No se toca la descripción: puede haber sido editada por un administrador.
                permisoExistente.get().setTipoPermiso(tipoPermiso);
                continue;
            }

            Permiso permiso = new Permiso();
            permiso.setNombre(permisoNombre);
            permiso.setDescripcion(seed.descripcion());
            permiso.setTipoPermiso(tipoPermiso);
            permisoRepository.save(permiso);
        }
    }
}