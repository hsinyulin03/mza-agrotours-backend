package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.notificacion.ScopeNotificacion;
import com.mza_agrotours.backend.enums.ScopeNotificacionNombre;
import com.mza_agrotours.backend.repositories.ScopeNotificacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(7)
public class ScopeNotificacionSeeder implements CommandLineRunner {
    private final ScopeNotificacionRepository scopeNotificacionRepository;

    public ScopeNotificacionSeeder(ScopeNotificacionRepository scopeNotificacionRepository) {
        this.scopeNotificacionRepository = scopeNotificacionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (ScopeNotificacionNombre nombre : ScopeNotificacionNombre.values()) {
            if (scopeNotificacionRepository.existsByNombre(nombre)) {
                continue;
            }
            ScopeNotificacion scope = new ScopeNotificacion();
            scope.setNombre(nombre);
            scopeNotificacionRepository.save(scope);
        }
    }
}
