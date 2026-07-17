package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.actividad.EstadoActividadDia;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadDiaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstadoActividadDiaSeeder implements CommandLineRunner {

    private final EstadoActividadDiaRepository estadoActividadDiaRepository;

    public EstadoActividadDiaSeeder(EstadoActividadDiaRepository estadoActividadDiaRepository) {
        this.estadoActividadDiaRepository = estadoActividadDiaRepository;
    }

    @Override
    public void run(String... args) {
        for (EstadoActividadDiaNombre nombre : EstadoActividadDiaNombre.values()) {
            if (!estadoActividadDiaRepository.existsByNombre(nombre)) {
                EstadoActividadDia estado = new EstadoActividadDia();
                estado.setNombre(nombre);
                estadoActividadDiaRepository.save(estado);
            }
        }
    }
}