package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.cuenta.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaisRepository extends JpaRepository<Pais, UUID> {
    Optional<Pais> findByNombre(String nombre);
    Optional<Pais> findByIso2(String iso2);
}
