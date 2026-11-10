package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.ManualUsuario;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManualUsuarioRepository extends BaseEntityRepository<ManualUsuario, UUID> {
    Optional<ManualUsuario> findFirstByOrderByIdAsc();
}
