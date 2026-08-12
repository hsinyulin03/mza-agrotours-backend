package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.dtos.productor.EstadoProductor;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;
import com.mza_agrotours.backend.repositories.EstadoProductorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstadoProductorSeeder implements CommandLineRunner {

    private final EstadoProductorRepository estadoProductorRepository;

    public EstadoProductorSeeder(EstadoProductorRepository estadoProductorRepository) {
        this.estadoProductorRepository = estadoProductorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (EstadoProductorNombre nombre : EstadoProductorNombre.values()) {
            if (estadoProductorRepository.existsByNombreAndFechaHoraBajaIsNull(nombre)) {
                continue;
            }

            EstadoProductor estado = new EstadoProductor();
            estado.setNombre(nombre);
            estadoProductorRepository.save(estado);
        }
    }
}