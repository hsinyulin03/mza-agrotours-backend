package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.deudas.EstadoDeudaEstablecimiento;
import com.mza_agrotours.backend.enums.EstadoDeudaEstablecimientoNombre;
import com.mza_agrotours.backend.repositories.EstadoDeudaEstablecimientoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstadoDeudaEstablecimientoSeeder implements CommandLineRunner {
    private final EstadoDeudaEstablecimientoRepository estadoDeudaEstablecimientoRepository;

    public EstadoDeudaEstablecimientoSeeder(EstadoDeudaEstablecimientoRepository estadoDeudaEstablecimientoRepository) {
        this.estadoDeudaEstablecimientoRepository = estadoDeudaEstablecimientoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (EstadoDeudaEstablecimientoNombre nombre : EstadoDeudaEstablecimientoNombre.values()) {
            if (estadoDeudaEstablecimientoRepository.existsByNombre(nombre)) {
                continue;
            }

            EstadoDeudaEstablecimiento estado = new EstadoDeudaEstablecimiento();
            estado.setNombre(nombre);
            estadoDeudaEstablecimientoRepository.save(estado);
        }
    }
}