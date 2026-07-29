package com.mza_agrotours.backend.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mza_agrotours.backend.dtos.DepartamentoSeed;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.repositories.DepartamentoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DepartamentoSeeder implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final DepartamentoRepository departamentoRepository;

    @Value("classpath:data/cabeceras-departamentales-georreferenciadas.json")
    private Resource departamentoResource;

    public DepartamentoSeeder(ObjectMapper objectMapper, DepartamentoRepository departamentoRepository) {
        this.objectMapper = objectMapper;
        this.departamentoRepository = departamentoRepository;
    }

    @Override
    public void run(String... args) {
        List<Departamento> departamentosToSave = new ArrayList<>();

        List<DepartamentoSeed> departamentos = this.readDepartamentos();
        List<Departamento> departamentosDB = this.departamentoRepository.findAll();
        Set<String> departamentosDBCode = new HashSet<>(departamentosDB.stream().map(Departamento::getNombre).toList());
        for(DepartamentoSeed departamento : departamentos) {
            if(!departamentosDBCode.add(departamento.nombre())) {
                continue;
            }

            Departamento newDepartamento = new Departamento();
            newDepartamento.setNombre(departamento.nombre());
            newDepartamento.setLat(departamento.lat());
            newDepartamento.setLon(departamento.lon());

            departamentosToSave.add(newDepartamento);
        }
        this.departamentoRepository.saveAll(departamentosToSave);
    }

    private List<DepartamentoSeed> readDepartamentos(){
        try(InputStream inputStream = departamentoResource.getInputStream()) {
            return this.objectMapper.readValue(inputStream, new TypeReference<List<DepartamentoSeed>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to read/parse JSON file");
        }
    }
}
