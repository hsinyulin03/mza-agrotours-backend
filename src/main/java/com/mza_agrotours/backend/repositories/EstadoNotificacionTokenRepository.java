package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.notificacion.EstadoNotificacionToken;
import com.mza_agrotours.backend.enums.EstadoNotificacionTokenNombre;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadoNotificacionTokenRepository extends BaseEntityRepository<EstadoNotificacionToken, UUID>{

    boolean existsByNombre(EstadoNotificacionTokenNombre nombre);
    Optional<EstadoNotificacionToken> findByNombre(EstadoNotificacionTokenNombre nombre);

}
