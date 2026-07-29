package com.mza_agrotours.backend.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mza_agrotours.backend.dtos.PaisSeed;
import com.mza_agrotours.backend.entities.Pais;
import com.mza_agrotours.backend.repositories.PaisRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Order(2)
public class PaisSeeder implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final PaisRepository paisRepository;

    @Value("classpath:data/paises.json")
    private Resource paisesResource;

    public PaisSeeder(PaisRepository paisRepository, ObjectMapper objectMapper) {
        this.paisRepository = paisRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Pais> paisesToSave = new ArrayList<>();

        List<PaisSeed> paisSeeds = this.readPaises();
        //TODO: obtener un dataset traducido al español
        List<Pais> paises = this.paisRepository.findAll();
        Set<String> paisesCode = new HashSet<>(paises.stream().map(Pais::getIso2).toList());

        for (PaisSeed paisSeed : paisSeeds) {
            if (!paisesCode.add(paisSeed.code())) {
                continue;
            }

            Pais newPais = new Pais();
            newPais.setNombre(paisSeed.nombre());
            newPais.setIso2(paisSeed.code());
            paisesToSave.add(newPais);
        }

        this.paisRepository.saveAll(paisesToSave);
    }

    public List<PaisSeed> readPaises() {
        try (InputStream inputStream = paisesResource.getInputStream()) {
            return this.objectMapper.readValue(inputStream, new TypeReference<List<PaisSeed>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to read/parse JSON file",e);
        }
    }
}
