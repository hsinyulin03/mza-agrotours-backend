package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.notificacion.TipoNotificacion;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipoNotificacionRepository extends BaseEntityRepository<TipoNotificacion, UUID>{
    boolean existsByNombre(TipoNotificacionNombre nombre);
    Optional<TipoNotificacion> findByNombre(TipoNotificacionNombre nombre);
}
