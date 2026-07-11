package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.TipoCultivo;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoCultivoRepository
        extends BaseEntityRepository<TipoCultivo, Long> {
}