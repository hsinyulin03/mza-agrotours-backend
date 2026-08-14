package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.roles_permisos.GrupoPermiso;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.repositories.GrupoPermisoRepository;
import com.mza_agrotours.backend.repositories.PermisoRepository;
import com.mza_agrotours.backend.repositories.TipoPermisoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@Component
@Order(6)
public class GrupoPermisoSeeder implements CommandLineRunner {

    /**
     * Datos iniciales de un grupo de permisoCodigos.
     */
    private record SeedGrupoPermiso(TipoPermisoNombre tipoPermiso,
                                    String descripcion,
                                    String icono,
                                    List<PermisoCodigo> permisoCodigos) {}

    /**
     * Valores iniciales de cada grupo. Al igual que en {@link PermisoSeeder} y {@link RolSeeder},
     * la descripcion es editable por un administrador en runtime, asi que solo se usa al crear el
     * grupo y despues la fuente de verdad es la base de datos. El tipo de permiso, el icono y los
     * permisoCodigos son estructurales y se sincronizan en cada arranque.
     */
    private static final Map<String, SeedGrupoPermiso> SEEDS = Map.ofEntries(
            entry("Gestión de administradores",
                    new SeedGrupoPermiso(
                            TipoPermisoNombre.ADMIN,
                            "Altas bajas y cambios de los roles de administradores del sistema.",
                            "user-cog",
                            List.of(PermisoCodigo.GESTIONAR_ADMIN,
                                    PermisoCodigo.LEER_ADMIN))),
            entry("Gestión de solicitudes de establecimientos",
                    new SeedGrupoPermiso(
                            TipoPermisoNombre.ADMIN,
                            "Revisar, aceptar o rechazar solicitudes de creación de establecimientos",
                            "clipboard-check",
                            List.of(PermisoCodigo.LEER_SOLICITUD_ESTABLECIMIENTO,
                                    PermisoCodigo.GESTIONAR_SOLICITUD_ESTABLECIMIENTO)))
    );

    private final GrupoPermisoRepository grupoPermisoRepository;
    private final PermisoRepository permisoRepository;
    private final TipoPermisoRepository tipoPermisoRepository;

    public GrupoPermisoSeeder(GrupoPermisoRepository grupoPermisoRepository,
                              PermisoRepository permisoRepository,
                              TipoPermisoRepository tipoPermisoRepository) {
        this.grupoPermisoRepository = grupoPermisoRepository;
        this.permisoRepository = permisoRepository;
        this.tipoPermisoRepository = tipoPermisoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Map.Entry<String, SeedGrupoPermiso> entrada : SEEDS.entrySet()) {
            String nombre = entrada.getKey();
            SeedGrupoPermiso seed = entrada.getValue();

            TipoPermiso tipoPermiso = this.tipoPermisoRepository.findByNombre(seed.tipoPermiso())
                    .orElseThrow(() -> new IllegalStateException(
                            "Tipo de permiso no encontrado: " + seed.tipoPermiso()
                                    + " (requerido por " + nombre + ")"));

            List<Permiso> permisos = this.resolvePermisos(nombre, seed);

            Optional<GrupoPermiso> grupoExistente = this.grupoPermisoRepository.findByNombre(nombre);

            if (grupoExistente.isPresent()) {
                GrupoPermiso grupoPermiso = grupoExistente.get();
                // No se toca la descripcion: puede haber sido editada por un administrador.
                grupoPermiso.setTipoPermiso(tipoPermiso);
                grupoPermiso.setIcono(seed.icono());
                grupoPermiso.setPermisos(permisos);
                continue;
            }

            GrupoPermiso grupoPermiso = new GrupoPermiso();
            grupoPermiso.setNombre(nombre);
            grupoPermiso.setDescripcion(seed.descripcion());
            grupoPermiso.setIcono(seed.icono());
            grupoPermiso.setTipoPermiso(tipoPermiso);
            grupoPermiso.setPermisos(permisos);
            this.grupoPermisoRepository.save(grupoPermiso);
        }
    }

    private List<Permiso> resolvePermisos(String grupoNombre, SeedGrupoPermiso seed) {
        return seed.permisoCodigos().stream()
                .map(codigo -> this.permisoRepository.findByCodigo(codigo)
                        .orElseThrow(() -> new IllegalStateException(
                                "Permiso no encontrado: " + codigo
                                        + " (requerido por " + grupoNombre + ")")))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
