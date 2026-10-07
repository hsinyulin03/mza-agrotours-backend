package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.notificacion.ScopeNotificacion;
import com.mza_agrotours.backend.entities.notificacion.TipoNotificacion;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
import com.mza_agrotours.backend.repositories.ScopeNotificacionRepository;
import com.mza_agrotours.backend.repositories.TipoNotificacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * El scope sale del enum y se sincroniza en cada arranque, asi un tipo que
 * cambia de bandeja no queda apuntando a la vieja.
 */
@Component
@Order(8)
public class TipoNotificacionSeeder implements CommandLineRunner {
    private final TipoNotificacionRepository tipoNotificacionRepository;
    private final ScopeNotificacionRepository scopeNotificacionRepository;

    public TipoNotificacionSeeder(TipoNotificacionRepository tipoNotificacionRepository,
                                  ScopeNotificacionRepository scopeNotificacionRepository) {
        this.tipoNotificacionRepository = tipoNotificacionRepository;
        this.scopeNotificacionRepository = scopeNotificacionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (TipoNotificacionNombre nombre : TipoNotificacionNombre.values()) {
            ScopeNotificacion scope = scopeNotificacionRepository.findByNombre(nombre.getScope())
                    .orElseThrow(() -> new IllegalStateException(
                            "Scope de notificación no encontrado: " + nombre.getScope()
                                    + " (requerido por " + nombre + ")"));

            TipoNotificacion tipo = tipoNotificacionRepository.findByNombre(nombre)
                    .orElseGet(TipoNotificacion::new);
            tipo.setNombre(nombre);
            tipo.setScopeNotificacion(scope);
            tipoNotificacionRepository.save(tipo);
        }
    }
}
