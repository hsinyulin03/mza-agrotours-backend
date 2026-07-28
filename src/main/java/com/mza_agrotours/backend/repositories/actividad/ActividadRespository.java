package com.mza_agrotours.backend.repositories.actividad;

import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface ActividadRespository extends BaseEntityRepository<Actividad, UUID> {
    Optional<Actividad>  findByNombreIgnoreCaseAndFechaHoraBajaIsNull(String nombre);
    Optional<Actividad> findByIdAndFechaHoraBajaIsNull(UUID id);

    @Query("SELECT a FROM Actividad a WHERE (:busqueda IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))) " +
            "AND (:estado IS NULL OR a.estado.nombre = :estado)")
    List<Actividad> findByFiltrosDinamicos(
            @Param("busqueda") String busqueda,
            @Param("estado") EstadoActividadNombre estado
    );

    //TODO - Ignoramos temporalmente los filtros de cultivo y departamento
    @Query("SELECT a FROM Actividad a " +
            "WHERE a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO " +
            "AND a.fechaHoraBaja IS NULL")
    List<Actividad> explorarActividadesPublicadas();

    @Query("SELECT MAX(ad.fechaHoraInicio) FROM Actividad a JOIN a.actividadesDias ad WHERE a.id = :actividadId")
    Optional<LocalDateTime> findUltimaFechaByActividadId(@Param("actividadId") UUID actividadId);
}
