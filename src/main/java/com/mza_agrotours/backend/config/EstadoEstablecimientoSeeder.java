package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.establecimiento.EstadoEstablecimiento;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.repositories.EstadoEstablecimientoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstadoEstablecimientoSeeder implements CommandLineRunner {

    private final EstadoEstablecimientoRepository estadoEstablecimientoRepository;

    public EstadoEstablecimientoSeeder(EstadoEstablecimientoRepository estadoEstablecimientoRepository) {
        this.estadoEstablecimientoRepository = estadoEstablecimientoRepository;
    }

    @Override
    public void run(String... args) {
        for (EstadoEstablecimientoNombre nombre : EstadoEstablecimientoNombre.values()) {
            if (!estadoEstablecimientoRepository.existsByNombre(nombre)) {
                EstadoEstablecimiento estado = new EstadoEstablecimiento();
                estado.setNombre(nombre);
                estadoEstablecimientoRepository.save(estado);
            }
        }
    }
}