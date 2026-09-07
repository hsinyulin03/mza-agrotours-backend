package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.notificacion.EstadoNotificacionToken;
import com.mza_agrotours.backend.enums.EstadoNotificacionTokenNombre;
import com.mza_agrotours.backend.repositories.EstadoNotificacionTokenRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstadoNotificacionTokenSeeder  implements CommandLineRunner {
    private final EstadoNotificacionTokenRepository estadoNotificacionTokenRepository;

    public EstadoNotificacionTokenSeeder(EstadoNotificacionTokenRepository estadoNotificacionTokenRepository) {
        this.estadoNotificacionTokenRepository = estadoNotificacionTokenRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (EstadoNotificacionTokenNombre nombre : EstadoNotificacionTokenNombre.values()) {
            if (estadoNotificacionTokenRepository.existsByNombre(nombre)) {
                continue;
            }
            EstadoNotificacionToken estado = new EstadoNotificacionToken();
            estado.setNombre(nombre);
            estadoNotificacionTokenRepository.save(estado);
        }
    }
}
