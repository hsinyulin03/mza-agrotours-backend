package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.deudas.DeudaPago;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeudaPagoRepository extends BaseEntityRepository<DeudaPago, UUID> {

    @Query("SELECT dp FROM DeudaPago dp " +
            "JOIN FETCH dp.pago " +
            "JOIN FETCH dp.cargadoPorAdmin a " +
            "JOIN FETCH a.usuario " +
            "WHERE dp.deudaEstablecimientoEstado.id IN :idsEstado")
    List<DeudaPago> findByEstadoIdIn(@Param("idsEstado") Collection<UUID> idsEstado);

    // El pago no tiene referencia a la deuda: se llega por los estados de la deuda
    @Query("SELECT COUNT(dp) > 0 FROM DeudaEstablecimiento d JOIN d.estados e, DeudaPago dp " +
            "WHERE dp.deudaEstablecimientoEstado = e " +
            "AND d.id = :idDeuda " +
            "AND dp.pago.idTransaccionExterna = :idPagoExterno")
    boolean existsByDeudaIdAndIdPagoExterno(@Param("idDeuda") UUID idDeuda, @Param("idPagoExterno") String idPagoExterno);
}
