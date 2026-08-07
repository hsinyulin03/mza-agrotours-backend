package com.mza_agrotours.backend.repositories.receta;

import com.mza_agrotours.backend.entities.receta.Receta;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecetaRepository extends BaseEntityRepository<Receta, UUID> {
    long countByFechaHoraBajaIsNull();

    boolean existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);

    Optional<Receta> findByIdAndFechaHoraBajaIsNull(UUID id);
    Optional<Receta> findByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);
}