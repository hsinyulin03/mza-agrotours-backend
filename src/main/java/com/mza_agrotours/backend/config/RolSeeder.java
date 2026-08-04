package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.RolProtegido;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.repositories.PermisoRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
import com.mza_agrotours.backend.repositories.TipoPermisoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(5)
public class RolSeeder implements CommandLineRunner {
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
    public void run(String... args) throws Exception {
        List<Rol> rolesSeed = new java.util.ArrayList<>(List.of(this.buildRolAdminLider()));
        rolesSeed.add(buildRolAdminPrueba());
        for (Rol rol : rolesSeed) {
            if (this.rolRepository.existsByNombre(rol.getNombre())) {
                continue;
            }
            this.rolRepository.save(rol);
        }
    }

    private Rol buildRolAdminLider() {
        TipoPermiso tipoPermisoAdmin = this.tipoPermisoRepository
                .findByNombre(TipoPermisoNombre.ADMIN).orElseThrow(
                        () -> new IllegalStateException("Tipo de permiso no encontrado: " + TipoPermisoNombre.ADMIN)
                );

        List<Permiso> permisosAdmin = this.permisoRepository
                .findByTipoPermiso(tipoPermisoAdmin);

        Rol rolAdmin = new Rol();
        rolAdmin.setNombre(RolProtegido.ADMIN_LIDER.getNombre());
        rolAdmin.setDescripcion("Rol administrador con todos los permisos");
        rolAdmin.setEsProtegido(true);
        rolAdmin.setPermisos(permisosAdmin);
        rolAdmin.setTipoPermiso(tipoPermisoAdmin);

        return rolAdmin;
    }

    private Rol buildRolAdminPrueba() {
        TipoPermiso tipoPermisoAdmin = this.tipoPermisoRepository
                .findByNombre(TipoPermisoNombre.ADMIN).orElseThrow(
                        () -> new IllegalStateException("Tipo de permiso no encontrado: " + TipoPermisoNombre.ADMIN)
                );

        List<Permiso> permisosAdmin = this.permisoRepository.findByTipoPermiso(tipoPermisoAdmin);

        Rol rolAdminPrueba = new Rol();
        rolAdminPrueba.setNombre("Admin prueba");
        rolAdminPrueba.setDescripcion("Rol administrador con todos los permisos");
        rolAdminPrueba.setEsProtegido(true);
        rolAdminPrueba.setPermisos(permisosAdmin);
        rolAdminPrueba.setTipoPermiso(tipoPermisoAdmin);

        return rolAdminPrueba;
    }
}
