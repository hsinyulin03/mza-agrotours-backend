package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.notificacion.TipoNotificacion;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
import com.mza_agrotours.backend.repositories.TipoNotificacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TipoNotificacionSeeder implements CommandLineRunner {
    private final TipoNotificacionRepository tipoNotificacionRepository;

    public TipoNotificacionSeeder(TipoNotificacionRepository tipoNotificacionRepository) {
        this.tipoNotificacionRepository = tipoNotificacionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (TipoNotificacionNombre nombre : TipoNotificacionNombre.values()) {
            if (tipoNotificacionRepository.existsByNombre(nombre)) {
                continue;
            }
            TipoNotificacion tipo = new TipoNotificacion();
            tipo.setNombre(nombre);
            tipoNotificacionRepository.save(tipo);
        }
    }
}