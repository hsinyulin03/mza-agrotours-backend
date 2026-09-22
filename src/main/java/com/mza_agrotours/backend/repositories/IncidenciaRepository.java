package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.incidencia.Incidencia;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenciaRepository extends BaseEntityRepository<Incidencia, UUID> {

    @Query("SELECT i FROM Incidencia i WHERE i.usuario.email = :email ORDER BY i.fechaHoraIncio ASC")
    List<Incidencia> findByUsuarioEmailOrderByFechaHoraIncioAsc(@Param("email") String email);
}
