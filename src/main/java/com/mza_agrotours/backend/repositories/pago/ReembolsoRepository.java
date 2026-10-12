package com.mza_agrotours.backend.repositories.pago;

import com.mza_agrotours.backend.dtos.pago.ReembolsoGetCatalogoDTO;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.enums.EstadoReembolsoNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
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

    // Reembolsos "Pedido" del establecimiento: los que la pasarela no pudo devolver y tiene que hacer el productor
    @Query(value = "SELECT new com.mza_agrotours.backend.dtos.pago.ReembolsoGetCatalogoDTO(" +
            "  r.id, r.fechaHoraPedido, r.montoReembolso, u.nombre, u.email, res.id, SIZE(res.reservaDetalles), a.nombre, ad.fechaHoraInicio) " +
            "FROM Reembolso r " +
            "JOIN r.reserva res " +
            "JOIN res.visitante v " +
            "JOIN v.usuario u " +
            "JOIN res.actividad a " +
            "JOIN res.actividadDia ad " +
            "WHERE a.establecimiento.id = :establecimientoId " +
            "AND r.estadoActual.estadoReembolso.nombre = com.mza_agrotours.backend.enums.EstadoReembolsoNombre.PEDIDO",
            countQuery = "SELECT COUNT(r) FROM Reembolso r " +
                    "WHERE r.reserva.actividad.establecimiento.id = :establecimientoId " +
                    "AND r.estadoActual.estadoReembolso.nombre = com.mza_agrotours.backend.enums.EstadoReembolsoNombre.PEDIDO")
    Page<ReembolsoGetCatalogoDTO> findPedidosByEstablecimiento(@Param("establecimientoId") UUID establecimientoId,
                                                               Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reembolso r " +
            "JOIN FETCH r.reserva res " +
            "JOIN FETCH res.pago " +
            "WHERE r.id = :reembolsoId " +
            "AND res.actividad.establecimiento.id = :establecimientoId")
    Optional<Reembolso> findByIdAndEstablecimiento(@Param("reembolsoId") UUID reembolsoId,
                                                   @Param("establecimientoId") UUID establecimientoId);

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
            "LEFT JOIN FETCH r.pagoReembolsoSistema " +
            "JOIN r.estadoActual estado " +
            "WHERE r.id = :id AND estado.estadoReembolso.nombre IN :estados")
    Optional<Reembolso> findByIdAndEstadoActualIn(@Param("id") UUID id, @Param("estados") Collection<EstadoReembolsoNombre> estados);
}
