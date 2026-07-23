package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.solicitud_establecimiento.EstadoSolicitudEstablecimiento;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.EstadoSolicitudEstablecimientoNombre;
import com.mza_agrotours.backend.repositories.EstadoSolicitudEstablecimientoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstadoSolicitudEstablecimientoSeeder implements CommandLineRunner {
    private final EstadoSolicitudEstablecimientoRepository estadoSolicitudEstablecimientoRepository;

    public EstadoSolicitudEstablecimientoSeeder(EstadoSolicitudEstablecimientoRepository estadoSolicitudEstablecimientoRepository) {
        this.estadoSolicitudEstablecimientoRepository = estadoSolicitudEstablecimientoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (EstadoSolicitudEstablecimientoNombre nombre : EstadoSolicitudEstablecimientoNombre.values()) {
            if(estadoSolicitudEstablecimientoRepository.existsByNombre(nombre.toString())) {
                continue;
            }
            EstadoSolicitudEstablecimiento estado = new EstadoSolicitudEstablecimiento();
            estado.setNombre(nombre);
            estadoSolicitudEstablecimientoRepository.save(estado);
        }
    }
}
