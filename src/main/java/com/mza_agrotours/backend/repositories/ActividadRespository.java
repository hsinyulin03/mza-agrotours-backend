package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.actividad.Actividad;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRespository extends BaseEntityRepository<Actividad, Long> {
    boolean existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);

    //TODO - Ignoramos temporalmente los filtros de cultivo y departamento
    @Query("SELECT a FROM Actividad a WHERE a.estado = 'PUBLICADO' AND a.fechaHoraBaja IS NULL")
    List<Actividad> explorarActividadesPublicadas();
}
