package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EstablecimientoRepository extends BaseEntityRepository<Establecimiento, UUID> {
    boolean existsByCuit(String cuit);

}