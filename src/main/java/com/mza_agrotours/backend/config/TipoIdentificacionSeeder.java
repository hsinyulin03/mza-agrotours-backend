package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.TipoIdentificacionNombre;
import com.mza_agrotours.backend.repositories.TipoIdentificacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class TipoIdentificacionSeeder implements CommandLineRunner {

    private final TipoIdentificacionRepository tipoIdentificacionRepository;

    public TipoIdentificacionSeeder(TipoIdentificacionRepository tipoIdentificacionRepository) {
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
    }

    @Override
    public void run(String... args) {
        for (TipoIdentificacionNombre nombre : TipoIdentificacionNombre.values()) {
            if (!tipoIdentificacionRepository.existsByNombre(nombre)) {
                TipoIdentificacion tipo = new TipoIdentificacion();
                tipo.setNombre(nombre);
                tipoIdentificacionRepository.save(tipo);
            }
        }
    }
}
