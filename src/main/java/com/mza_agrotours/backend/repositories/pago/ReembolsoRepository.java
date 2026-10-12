package com.mza_agrotours.backend.repositories.pago;

import com.mza_agrotours.backend.dtos.pago.ReembolsoGetCatalogoDTO;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
}
