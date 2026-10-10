package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.pago.EstadoReembolso;
import com.mza_agrotours.backend.enums.EstadoReembolsoNombre;
import com.mza_agrotours.backend.repositories.pago.EstadoReembolsoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstadoReembolsoSeeder implements CommandLineRunner {
    private final EstadoReembolsoRepository estadoReembolsoRepository;

    public EstadoReembolsoSeeder(EstadoReembolsoRepository estadoReembolsoRepository) {
        this.estadoReembolsoRepository = estadoReembolsoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (EstadoReembolsoNombre nombre : EstadoReembolsoNombre.values()) {
            if (estadoReembolsoRepository.existsByNombre(nombre)) {
                continue;
            }

            EstadoReembolso estado = new EstadoReembolso();
            estado.setNombre(nombre);
            estadoReembolsoRepository.save(estado);
        }
    }
}