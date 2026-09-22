package com.mza_agrotours.backend.repositories;


import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.clima.ClimaDptoDia;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClimaRepository extends BaseEntityRepository<ClimaDptoDia, UUID>{
    List<ClimaDptoDia> findByDepartamento(Departamento departamento);

    @Transactional
    @Modifying
    @Query("delete from ClimaDptoDia c where c.fecha < :fecha")
    int eliminarAnterioresA(@Param("fecha") LocalDate fecha);
}
