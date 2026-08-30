package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.notificacion.Notificacion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends BaseEntityRepository<Notificacion, UUID> {

    @Query("select n from Notificacion n " +
            "where n.destinatario = :destinatario " +
            "and ((:estId is null and n.establecimiento is null) " +
            "     or n.establecimiento.id = :estId) " +
            "order by n.fechaHoraAlta desc")
    List<Notificacion> listarNotificaciones(@Param("destinatario") Usuario destinatario,
                                            @Param("estId") UUID estId);

    @Query("select count(n) from Notificacion n " +
            "where n.destinatario = :destinatario " +
            "and n.fechaHoraLectura is null " +
            "and ((:estId is null and n.establecimiento is null) " +
            "or n.establecimiento.id = :estId)")
    long contarNoLeidas(@Param("destinatario") Usuario destinatario,
                        @Param("estId") UUID estId);

    @Query("select n from Notificacion n " +
            "where n.id = :id " +
            "and n.destinatario = :destinatario " +
            "and ((:estId is null and n.establecimiento is null) " +
            "or n.establecimiento.id = :estId)")
    Optional<Notificacion> findNotificacionById(@Param("id") UUID id,
                                                @Param("destinatario") Usuario destinatario,
                                                @Param("estId") UUID estId);

}
