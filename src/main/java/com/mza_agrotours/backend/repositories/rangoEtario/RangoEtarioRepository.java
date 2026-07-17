package com.mza_agrotours.backend.repositories.rangoEtario;

import com.mza_agrotours.backend.entities.RangoEtario;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RangoEtarioRepository extends BaseEntityRepository<RangoEtario, UUID> {
    boolean existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);
    Optional<RangoEtario> findByIdAndFechaHoraBajaIsNull(UUID id);
    List<RangoEtario> findAllByFechaHoraBajaIsNull();
}