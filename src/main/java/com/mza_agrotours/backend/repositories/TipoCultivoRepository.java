package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.TipoCultivo;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TipoCultivoRepository
        extends BaseEntityRepository<TipoCultivo, UUID> {
}
