package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.entities.faq.CategoriaFAQ;
import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import com.mza_agrotours.backend.repositories.faq.CategoriaFaqRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CategoriaFaqSeeder implements CommandLineRunner {

    private final CategoriaFaqRepository categoriaFaqRepository;

    public CategoriaFaqSeeder(CategoriaFaqRepository categoriaFaqRepository) {
        this.categoriaFaqRepository = categoriaFaqRepository;
    }

    @Override
    public void run(String... args) {
        for (CategoriaFAQNombre nombre : CategoriaFAQNombre.values()) {
            if (categoriaFaqRepository.findByNombre(nombre).isEmpty()) {
                CategoriaFAQ categoria = new CategoriaFAQ();
                categoria.setNombre(nombre);
                categoriaFaqRepository.save(categoria);
            }
        }
    }
}
