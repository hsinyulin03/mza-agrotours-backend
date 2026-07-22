package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre;
import com.mza_agrotours.backend.repositories.EstadoReservaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstadoReservaSeeder implements CommandLineRunner {
    private final EstadoReservaRepository estadoReservaRepository;

    public EstadoReservaSeeder(EstadoReservaRepository estadoReservaRepository) {
        this.estadoReservaRepository = estadoReservaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for ( EstadoReservaNombre nombre : EstadoReservaNombre.values()) {
            if (estadoReservaRepository.existsByNombre(nombre)) {
                continue;
            }

            EstadoReserva estado = new EstadoReserva();
            estado.setNombre(nombre);
            estadoReservaRepository.save(estado);
        }
    }
}
