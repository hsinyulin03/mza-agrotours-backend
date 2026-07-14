package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.TipoIdentificacionNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipoIdentificacionRepository extends JpaRepository<TipoIdentificacion, UUID> {
    Optional<TipoIdentificacion> findByNombre(TipoIdentificacionNombre nombre);
    boolean existsByNombre(TipoIdentificacionNombre nombre);
}
