package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.incidencia.EstadoIncidencia;
import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import com.mza_agrotours.backend.repositories.EstadoIncidenciaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstadoIncidenciaSeeder implements CommandLineRunner {
    private final EstadoIncidenciaRepository estadoIncidenciaRepository;

    public EstadoIncidenciaSeeder(EstadoIncidenciaRepository estadoIncidenciaRepository) {
        this.estadoIncidenciaRepository = estadoIncidenciaRepository;
    }

    @Override
    public void run(String... args) {
        for (EstadoIncidenciaNombre nombre : EstadoIncidenciaNombre.values()) {
            if (estadoIncidenciaRepository.existsByNombre(nombre)) {
                continue;
            }

            EstadoIncidencia estado = new EstadoIncidencia();
            estado.setNombre(nombre);
            estadoIncidenciaRepository.save(estado);
        }
    }
}
