package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.cultivo.Estacionalidad;
import com.mza_agrotours.backend.enums.EstacionalidadNombre;
import com.mza_agrotours.backend.repositories.TipoCultivo.EstacionalidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EstacionalidadSeeder implements CommandLineRunner {

    @Autowired
    private EstacionalidadRepository estacionalidadRepository;

    @Override
    public void run(String... args) {
        for (EstacionalidadNombre nombre : EstacionalidadNombre.values()) {
            if (estacionalidadRepository.findByNombre(nombre).isEmpty()) {
                Estacionalidad e = new Estacionalidad();
                e.setNombre(nombre);
                e.setColorMuestra(colorPorDefecto(nombre));
                estacionalidadRepository.save(e);
            }
        }
    }

    private String colorPorDefecto(EstacionalidadNombre nombre) {
        return switch (nombre) {
            case COSECHA -> "#154212";
            case CRECIMIENTO -> "#C9A227";
            case REPOSO -> "#8C7A55";
        };
    }
}