package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.deudas.DeudaEstablecimiento;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeudaRepository extends BaseEntityRepository<DeudaEstablecimiento, UUID>{

    @Query("SELECT d FROM DeudaEstablecimiento d " +
            "JOIN FETCH d.establecimientoDeudor " +
            "JOIN FETCH d.estadoActual ea " +
            "JOIN FETCH ea.estadoDeudaEstablecimiento " +
            "ORDER BY d.fechaCreacionDeuda ASC")
    List<DeudaEstablecimiento> findAllConEstablecimientoYEstado();
}