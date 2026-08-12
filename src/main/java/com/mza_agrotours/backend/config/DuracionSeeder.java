package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.receta.Duracion;
import com.mza_agrotours.backend.enums.DuracionNombre;
import com.mza_agrotours.backend.repositories.receta.DuracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DuracionSeeder implements CommandLineRunner {

    @Autowired
    private DuracionRepository duracionRepository;

    @Override
    public void run(String... args) {
        crearSiNoExiste(DuracionNombre.RAPIDA, 0, 45);
        crearSiNoExiste(DuracionNombre.MEDIA, 46, 120);
        crearSiNoExiste(DuracionNombre.LARGA, 121, null);
    }

    private void crearSiNoExiste(DuracionNombre nombre, Integer minDesde, Integer minHasta) {
        if (duracionRepository.findByNombre(nombre).isEmpty()) {
            Duracion duracion = new Duracion();
            duracion.setNombre(nombre);
            duracion.setMinDesde(minDesde);
            duracion.setMinHasta(minHasta);
            duracionRepository.save(duracion);
        }
    }
}