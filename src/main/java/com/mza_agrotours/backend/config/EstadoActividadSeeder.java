package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.actividad.EstadoActividad;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstadoActividadSeeder implements CommandLineRunner {

    private final EstadoActividadRepository estadoActividadRepository;

    public EstadoActividadSeeder(EstadoActividadRepository estadoActividadRepository) {
        this.estadoActividadRepository = estadoActividadRepository;
    }

    @Override
    public void run(String... args) {
        for (EstadoActividadNombre nombre : EstadoActividadNombre.values()) {
            if (!estadoActividadRepository.existsByNombre(nombre)) {
                EstadoActividad estado = new EstadoActividad();
                estado.setNombre(nombre);
                estadoActividadRepository.save(estado);
            }
        }
    }
}