package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Archivo;

import java.util.UUID;

public interface ArchivoRepository extends BaseEntityRepository<Archivo, UUID> {
    boolean existsByKey(String key);
}
