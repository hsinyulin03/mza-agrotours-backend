package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.notificacion.ScopeNotificacion;
import com.mza_agrotours.backend.enums.ScopeNotificacionNombre;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScopeNotificacionRepository extends BaseEntityRepository<ScopeNotificacion, UUID> {
    boolean existsByNombre(ScopeNotificacionNombre nombre);
    Optional<ScopeNotificacion> findByNombre(ScopeNotificacionNombre nombre);
}
