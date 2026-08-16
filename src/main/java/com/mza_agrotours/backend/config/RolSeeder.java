package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.RolProtegido;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.repositories.PermisoRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
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
@Order(5)
public class RolSeeder implements CommandLineRunner {

    /**
     * Datos iniciales de un rol. Un {@code permisoCodigos} nulo significa "todos los permisoCodigos del
     * tipo", que es como se definen los roles lideres: al agregar un permiso nuevo al enum,
     * el rol lider lo toma solo en el siguiente arranque.
     */
    private record SeedRol(TipoPermisoNombre tipoPermiso,
                           String descripcion,
                           boolean esProtegido,
                           List<PermisoCodigo> permisoCodigos) {

        static SeedRol conTodosLosPermisos(TipoPermisoNombre tipoPermiso,
                                           String descripcion,
                                           boolean esProtegido) {
            return new SeedRol(tipoPermiso, descripcion, esProtegido, null);
        }
    }

    /**
     * Valores iniciales de cada rol. Al igual que en {@link PermisoSeeder}, la descripcion es
     * editable por un administrador en runtime, asi que solo se usa al crear el rol y despues
     * la fuente de verdad es la base de datos. El tipo de permiso, el flag de protegido y los
     * permisoCodigos son estructurales y se sincronizan en cada arranque.
     */
    private static final Map<String, SeedRol> SEEDS = Map.ofEntries(
            entry(RolProtegido.ADMIN_LIDER.getNombre(),
                    SeedRol.conTodosLosPermisos(
                            TipoPermisoNombre.ADMIN,
                            "Rol administrador con todos los permisoCodigos",
                            true)),
            entry("Admin prueba",
                    SeedRol.conTodosLosPermisos(
                            TipoPermisoNombre.ADMIN,
                            "Rol administrador con todos los permisoCodigos",
                            false)),
            entry(RolProtegido.PRODUCTOR_LIDER.getNombre(),
                    SeedRol.conTodosLosPermisos(
                            TipoPermisoNombre.PRODUCTOR,
                            "Rol productor con todos los permisoCodigos",
                            true))
    );

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final TipoPermisoRepository tipoPermisoRepository;

    public RolSeeder(RolRepository rolRepository,
                     PermisoRepository permisoRepository,
                     TipoPermisoRepository tipoPermisoRepository) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.tipoPermisoRepository = tipoPermisoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Map.Entry<String, SeedRol> entrada : SEEDS.entrySet()) {
            String nombre = entrada.getKey();
            SeedRol seed = entrada.getValue();

            TipoPermiso tipoPermiso = this.tipoPermisoRepository.findByNombre(seed.tipoPermiso())
                    .orElseThrow(() -> new IllegalStateException(
                            "Tipo de permiso no encontrado: " + seed.tipoPermiso()
                                    + " (requerido por " + nombre + ")"));

            List<Permiso> permisos = this.resolvePermisos(nombre, seed, tipoPermiso);

            Optional<Rol> rolExistente = this.rolRepository.findByNombre(nombre);

            if (rolExistente.isPresent()) {
                Rol rol = rolExistente.get();
                // No se toca la descripcion: puede haber sido editada por un administrador.
                rol.setTipoPermiso(tipoPermiso);
                rol.setEsProtegido(seed.esProtegido());
                rol.setPermisos(permisos);
                continue;
            }

            Rol rol = new Rol();
            rol.setNombre(nombre);
            rol.setDescripcion(seed.descripcion());
            rol.setEsProtegido(seed.esProtegido());
            rol.setTipoPermiso(tipoPermiso);
            rol.setPermisos(permisos);
            this.rolRepository.save(rol);
        }
    }

    private List<Permiso> resolvePermisos(String rolNombre, SeedRol seed, TipoPermiso tipoPermiso) {
        if (seed.permisoCodigos() == null) {
            return new ArrayList<>(this.permisoRepository.findByTipoPermiso(tipoPermiso));
        }

        return seed.permisoCodigos().stream()
                .map(codigo -> this.permisoRepository.findByCodigo(codigo)
                        .orElseThrow(() -> new IllegalStateException(
                                "Permiso no encontrado: " + codigo
                                        + " (requerido por " + rolNombre + ")")))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}