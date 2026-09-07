package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.notificacion.TokenNotificacion;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenNotificacionRepository extends BaseEntityRepository<TokenNotificacion, UUID> {
    List<TokenNotificacion> findByUsuarioAndFechaHoraBajaIsNull(Usuario usuario);
    Optional<TokenNotificacion> findByToken(String token);
}
