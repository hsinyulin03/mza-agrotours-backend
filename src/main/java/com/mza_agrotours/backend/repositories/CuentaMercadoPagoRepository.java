package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CuentaMercadoPagoRepository extends BaseEntityRepository<CuentaMercadoPago, UUID> {
    // Para resolver el vendedor de un pago que llega por webhook (collector_id)
    List<CuentaMercadoPago> findByMpUserIdAndFechaHoraBajaIsNull(Long mpUserId);

    // Para el job de renovación de tokens. También se filtra por establecimiento vigente
    // por si alguna baja de establecimiento no desvinculó su cuenta.
    @Query("SELECT c FROM CuentaMercadoPago c " +
            "WHERE c.fechaHoraBaja IS NULL " +
            "AND c.establecimiento.fechaHoraBaja IS NULL " +
            "AND c.fechaHoraExpiracionToken < :limite")
    List<CuentaMercadoPago> findARenovar(@Param("limite") LocalDateTime limite);
}