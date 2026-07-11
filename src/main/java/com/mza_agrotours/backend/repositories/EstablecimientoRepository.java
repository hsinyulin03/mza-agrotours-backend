package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import org.springframework.stereotype.Repository;

@Repository
public interface EstablecimientoRepository extends BaseEntityRepository<Establecimiento, Long> {
    boolean existsByCuit(Long cuit);

}