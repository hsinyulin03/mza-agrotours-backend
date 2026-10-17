package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.deudas.DeudaEstablecimiento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeudaRepository extends BaseEntityRepository<DeudaEstablecimiento, UUID>{

    @Query("SELECT d FROM DeudaEstablecimiento d " +
            "JOIN FETCH d.establecimientoDeudor " +
            "JOIN FETCH d.estadoActual ea " +
            "JOIN FETCH ea.estadoDeudaEstablecimiento " +
            "ORDER BY d.fechaCreacionDeuda ASC")
    List<DeudaEstablecimiento> findAllConEstablecimientoYEstado();

    @Query("SELECT d FROM DeudaEstablecimiento d " +
            "JOIN FETCH d.establecimientoDeudor " +
            "LEFT JOIN FETCH d.estados e " +
            "LEFT JOIN FETCH e.estadoDeudaEstablecimiento " +
            "WHERE d.id = :id")
    Optional<DeudaEstablecimiento> findDetalleById(@Param("id") UUID id);

    // Bloquea la deuda para que dos administradores no registren devoluciones sobre el mismo monto restante
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DeudaEstablecimiento d WHERE d.id = :id")
    Optional<DeudaEstablecimiento> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT d.id FROM DeudaEstablecimiento d WHERE d.reembolso.id = :idReembolso")
    Optional<UUID> findIdByReembolsoId(@Param("idReembolso") UUID idReembolso);

    // Cada fila es [idReembolso, idDeuda]
    @Query("SELECT d.reembolso.id, d.id FROM DeudaEstablecimiento d WHERE d.reembolso.id IN :idsReembolso")
    List<Object[]> findIdsByReembolsoIdIn(@Param("idsReembolso") Collection<UUID> idsReembolso);
}