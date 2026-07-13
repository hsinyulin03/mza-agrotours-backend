package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.RangoEtario;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RangoEtarioRepository extends BaseEntityRepository<RangoEtario, Long>{
    boolean existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);
    Optional<RangoEtario> findByIdAndFechaHoraBajaIsNull(Long id);

    //Asegura que siempre haya solo una categoría de rango etario como tarifa base
    @Modifying
    @Query("UPDATE RangoEtario r SET r.esTarifaBase = false WHERE r.esTarifaBase = true")
    void desmarcarTarifasBaseExistentes();
}