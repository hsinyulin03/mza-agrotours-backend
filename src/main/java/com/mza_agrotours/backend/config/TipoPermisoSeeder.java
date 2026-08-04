package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.repositories.TipoPermisoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class TipoPermisoSeeder implements CommandLineRunner {
    private final TipoPermisoRepository tipoPermisoRepository;

    public TipoPermisoSeeder(TipoPermisoRepository tipoPermisoRepository) {
        this.tipoPermisoRepository = tipoPermisoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (TipoPermisoNombre nombre : TipoPermisoNombre.values()) {
            if (tipoPermisoRepository.existsByNombre(nombre)) {
                continue;
            }

            TipoPermiso tipoPermiso = new TipoPermiso();
            tipoPermiso.setNombre(nombre);
            tipoPermisoRepository.save(tipoPermiso);
        }

    }
}
