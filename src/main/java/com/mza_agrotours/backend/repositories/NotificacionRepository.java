package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.notificacion.Notificacion;
import com.mza_agrotours.backend.entities.notificacion.ScopeNotificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends BaseEntityRepository<Notificacion, UUID> {

    @Query(value= """
    SELECT n FROM Notificacion n
    WHERE n.destinatario = :destinatario
    AND n.scopeNotificacion = :scope
    AND (:estId IS NULL OR n.establecimiento.id = :estId)
    ORDER BY n.fechaHoraAlta DESC
    """,
    countQuery = """
    SELECT COUNT(n) FROM Notificacion n
    WHERE n.destinatario = :destinatario
    AND n.scopeNotificacion = :scope
    AND (:estId IS NULL OR n.establecimiento.id = :estId)
    """)
    Page<Notificacion> listarNotificaciones(@Param("destinatario") Usuario destinatario,
                                            @Param("scope") ScopeNotificacion scope,
                                            @Param("estId") UUID estId,
                                            Pageable pageable);

    @Query("select count(n) from Notificacion n " +
            "where n.destinatario = :destinatario " +
            "and n.fechaHoraLectura is null " +
            "and n.scopeNotificacion = :scope " +
            "and (:estId is null or n.establecimiento.id = :estId)")
    long contarNoLeidas(@Param("destinatario") Usuario destinatario,
                        @Param("scope") ScopeNotificacion scope,
                        @Param("estId") UUID estId);

    @Query("select n from Notificacion n " +
            "where n.id = :id " +
            "and n.destinatario = :destinatario " +
            "and n.scopeNotificacion = :scope " +
            "and (:estId is null or n.establecimiento.id = :estId)")
    Optional<Notificacion> findNotificacionById(@Param("id") UUID id,
                                                @Param("destinatario") Usuario destinatario,
                                                @Param("scope") ScopeNotificacion scope,
                                                @Param("estId") UUID estId);


    @Query("select n from Notificacion n " +
            "where n.destinatario = :destinatario " +
            "and n.scopeNotificacion = :scope " +
            "and (:establecimientoId IS NULL OR n.establecimiento.id = :establecimientoId) " +
            "and n.fechaHoraLectura IS NULL " +
            "and n.fechaHoraAlta <= :fechaHasta ")
    List<Notificacion> findAllNoLeidasHastaFechaByDestinatarioAndEstablecimientoId(@Param("fechaHasta") LocalDateTime fechaHasta,
                                                                                    @Param("destinatario") Usuario destinatario,
                                                                                    @Param("scope") ScopeNotificacion scope,
                                                                                    @Param("establecimientoId") UUID establecimientoId);
}
