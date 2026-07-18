package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.TipoCultivo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TipoCultivoRepository
        extends BaseEntityRepository<TipoCultivo, UUID> {
    @Query("""
        SELECT c FROM TipoCultivo c
        WHERE c.id IN :ids
        AND c.fechaHoraBaja IS NULL
        """)
    List<TipoCultivo> findActivosByIds(@Param("ids") List<UUID> ids);
}
