package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.RangoEtario;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RangoEtarioRepository extends BaseEntityRepository<RangoEtario, Long>{
    boolean existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);
    Optional<RangoEtario> findByIdAndFechaHoraBajaIsNull(Long id);
}