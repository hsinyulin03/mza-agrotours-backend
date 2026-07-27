package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.establecimiento.EstadoEstablecimiento;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadoEstablecimientoRepository extends JpaRepository<EstadoEstablecimiento, UUID> {
    Optional<EstadoEstablecimiento> findByNombreAndFechaBajaIsNull(EstadoEstablecimientoNombre nombre);

}