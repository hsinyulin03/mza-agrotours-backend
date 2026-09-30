package com.mza_agrotours.backend.repositories.pago;

import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
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
}