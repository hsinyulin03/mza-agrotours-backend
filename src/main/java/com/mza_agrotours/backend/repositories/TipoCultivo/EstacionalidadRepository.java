package com.mza_agrotours.backend.repositories.TipoCultivo;

import com.mza_agrotours.backend.entities.cultivo.Estacionalidad;
import com.mza_agrotours.backend.enums.EstacionalidadNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface EstacionalidadRepository extends BaseEntityRepository<Estacionalidad, UUID> {

    Optional<Estacionalidad> findByNombre(EstacionalidadNombre nombre);
}
