package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.enums.EstadoPagoNombre;
import com.mza_agrotours.backend.repositories.pago.EstadoPagoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstadoPagoSeeder implements CommandLineRunner {
    private final EstadoPagoRepository estadoPagoRepository;

    public EstadoPagoSeeder(EstadoPagoRepository estadoPagoRepository) {
        this.estadoPagoRepository = estadoPagoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (EstadoPagoNombre nombre : EstadoPagoNombre.values()) {
            if (estadoPagoRepository.existsByNombre(nombre)) {
                continue;
            }

            EstadoPago estado = new EstadoPago();
            estado.setNombre(nombre);
            estadoPagoRepository.save(estado);
        }
    }
}