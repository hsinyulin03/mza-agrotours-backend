package com.mza_agrotours.backend.repositories.TipoCultivo;

import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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
    boolean existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);

    Optional<TipoCultivo> findByIdAndFechaHoraBajaIsNull(UUID id);
    Optional<TipoCultivo> findByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);
    List<TipoCultivo> findByRecetasId(UUID recetaId);
    @Query("SELECT COUNT(DISTINCT tc) FROM TipoCultivo tc JOIN tc.recetas r WHERE r.fechaHoraBaja IS NULL")
    long contarCultivosConRecetaActiva();


}
