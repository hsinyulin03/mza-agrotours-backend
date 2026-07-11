package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Departamento;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartamentoRepository
        extends BaseEntityRepository<Departamento, Long> {
}