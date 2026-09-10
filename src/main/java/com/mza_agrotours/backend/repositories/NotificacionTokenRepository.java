package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.notificacion.Notificacion;
import com.mza_agrotours.backend.entities.notificacion.NotificacionToken;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionTokenRepository extends BaseEntityRepository<NotificacionToken, UUID> {
}