package com.mza_agrotours.backend.repositories.pago;

import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.enums.EstadoReembolsoNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReembolsoRepository extends BaseEntityRepository<Reembolso, UUID>{

    @Query("SELECT r FROM Reembolso r " +
            "JOIN FETCH r.reserva res " +
            "JOIN FETCH res.pago " +
            "JOIN r.estadoActual estado " +
            "WHERE estado.estadoReembolso.nombre = com.mza_agrotours.backend.enums.EstadoReembolsoNombre.EN_PROCESO")
    List<Reembolso> findReembolsosEnProceso();

    // Los más antiguos primero: son los que más tiempo llevan esperando
    @Query("SELECT r FROM Reembolso r " +
            "JOIN FETCH r.reserva res " +
            "JOIN FETCH res.actividad act " +
            "JOIN FETCH act.establecimiento est " +
            "LEFT JOIN FETCH est.titular tit " +
            "LEFT JOIN FETCH tit.usuario " +
            "JOIN r.estadoActual estado " +
            "WHERE estado.estadoReembolso.nombre IN :estados " +
            "ORDER BY estado.fechaHoraInicio ASC")
    List<Reembolso> findByEstadoActualIn(@Param("estados") Collection<EstadoReembolsoNombre> estados);

    @Query("SELECT r FROM Reembolso r " +
            "JOIN FETCH r.reserva res " +
            "JOIN FETCH res.actividad act " +
            "JOIN FETCH res.actividadDia " +
            "JOIN FETCH act.establecimiento est " +
            "LEFT JOIN FETCH est.titular tit " +
            "LEFT JOIN FETCH tit.usuario " +
            "JOIN FETCH res.visitante vis " +
            "JOIN FETCH vis.usuario " +
            "JOIN r.estadoActual estado " +
            "WHERE r.id = :id AND estado.estadoReembolso.nombre IN :estados")
    Optional<Reembolso> findByIdAndEstadoActualIn(@Param("id") UUID id, @Param("estados") Collection<EstadoReembolsoNombre> estados);
}
