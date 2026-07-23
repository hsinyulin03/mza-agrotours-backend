package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Departamento;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DepartamentoRepository
        extends BaseEntityRepository<Departamento, UUID> {
}