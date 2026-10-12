package com.mza_agrotours.backend.repositories.pago;

import com.mza_agrotours.backend.entities.pago.ReembolsoSistemaPago;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReembolsoSistemaPagoRepository extends BaseEntityRepository<ReembolsoSistemaPago, UUID> {

    @Query("SELECT rsp FROM ReembolsoSistemaPago rsp " +
            "JOIN FETCH rsp.pago " +
            "JOIN FETCH rsp.cargadoPorAdmin adm " +
            "JOIN FETCH adm.usuario " +
            "WHERE rsp.reembolso.id = :idReembolso")
    Optional<ReembolsoSistemaPago> findByReembolsoId(@Param("idReembolso") UUID idReembolso);
}
