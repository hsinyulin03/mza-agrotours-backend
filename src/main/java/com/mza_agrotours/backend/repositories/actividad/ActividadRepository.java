package com.mza_agrotours.backend.repositories.actividad;

import com.mza_agrotours.backend.dtos.actividad.DTOFiltro;
import com.mza_agrotours.backend.dtos.administrador_sistemas.ConteoPorEstablecimientoDTO;
import com.mza_agrotours.backend.dtos.reservas.DiaActividadReservaDTO;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ActividadRepository extends BaseEntityRepository<Actividad, UUID> {
    @Query("select count(a) > 0 from Actividad a " +
            "where lower(a.nombre) = lower(:nombre) " +
            "and a.establecimiento.id = :establecimientoId " +
            "and a.fechaHoraBaja is null " +
            "and (:idActividadActual is null or a.id <> :idActividadActual)")
    boolean existeOtraActividadConNombre(@Param("nombre") String nombre,
                                         @Param("establecimientoId") UUID establecimientoId,
                                         @Param("idActividadActual") UUID idActividadActual);
    Optional<Actividad> findByIdAndFechaHoraBajaIsNull(UUID id);

    @Query("SELECT a FROM Actividad a WHERE a.establecimiento.id = :establecimientoId " +
            "AND (:busqueda IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))) " +
            "AND (:estado IS NULL OR a.estado.nombre = :estado)")
    List<Actividad> findByFiltrosDinamicos(
            @Param("establecimientoId") UUID establecimientoId,
            @Param("busqueda") String busqueda,
            @Param("estado") EstadoActividadNombre estado
    );

    @Query("SELECT DISTINCT a FROM Actividad a " +
            "LEFT JOIN a.cultivos c " +
            "WHERE a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO " +
            "AND a.fechaHoraBaja IS NULL " +
            "AND a.establecimiento.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO " +
            "AND (:departamentoId IS NULL OR a.establecimiento.departamento.id = :departamentoId) " +
            "AND (:cultivosIds IS NULL OR c.id IN :cultivosIds )")
    List<Actividad> explorarActividadesPublicadas(@Param("cultivosIds") List <UUID> cultivosIds,
                                                  @Param("departamentoId") UUID departamentoId);

    @Query("SELECT MAX(ad.fechaHoraInicio) FROM Actividad a JOIN a.actividadesDias ad WHERE a.id = :actividadId")
    Optional<LocalDateTime> findUltimaFechaByActividadId(@Param("actividadId") UUID actividadId);

    @Query("SELECT a FROM Actividad a " +
            "JOIN a.actividadesDias ad " +
            "WHERE ad.id = :uuid")
    Optional<Actividad> getActividadByDiaActividadId(@Param("uuid") UUID uuidDiaActividad);

    @Query("SELECT NEW com.mza_agrotours.backend.dtos.reservas.DiaActividadReservaDTO(" +
            "CAST(ad.id AS string), ad.cuposMax, CAST(COUNT(rd) as int), ad.fechaHoraInicio, ad.fechaHoraFin) " +
            "FROM Actividad a JOIN a.actividadesDias ad " +
            "LEFT JOIN Reserva r ON  r.actividadDia = ad " +
            "AND r.estadoActual.estadoReserva.nombre IN (com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre.PENDIENTE, com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre.PAGADA) " +
            "LEFT JOIN r.reservaDetalles rd " +
            "WHERE a.id = :uuid " +
            "AND ad.estadoActual.estado.nombre IN (com.mza_agrotours.backend.enums.EstadoActividadDiaNombre.ACTIVA,com.mza_agrotours.backend.enums.EstadoActividadDiaNombre.REPROGRAMADA)" +
            "GROUP BY ad.id, ad.cuposMax, ad.fechaHoraInicio, ad.fechaHoraFin")
    List<DiaActividadReservaDTO> getDiaActividadReservaDTO(@Param("uuid") UUID uuidActividad);

    //Filtro de estado de actividad de un establecimiento
    @Query("SELECT NEW com.mza_agrotours.backend.dtos.actividad.DTOFiltro(a.estado.nombre, COUNT(a)) " +
            "FROM Actividad a WHERE a.establecimiento.id = :establecimientoId GROUP BY a.estado.nombre")
    List<DTOFiltro> contarActividadesPorEstado(@Param("establecimientoId") UUID establecimientoId);

    //Filtro de Departamentos
    @Query("SELECT NEW com.mza_agrotours.backend.dtos.actividad.DTOFiltro(d.id, d.nombre, COUNT(a)) " +
            "FROM Actividad a JOIN a.establecimiento.departamento d " +
            "WHERE a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO " +
            "AND a.fechaHoraBaja IS NULL " +
            "GROUP BY d.id, d.nombre " +
            "ORDER BY d.nombre ASC")
    List<DTOFiltro> obtenerFiltroDepartamentos();

    // Filtro de Cultivos
    @Query("SELECT NEW com.mza_agrotours.backend.dtos.actividad.DTOFiltro(c.id, c.nombre, COUNT(a)) " +
            "FROM Actividad a JOIN a.cultivos c " +
            "WHERE a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO " +
            "AND a.fechaHoraBaja IS NULL " +
            "GROUP BY c.id, c.nombre " +
            "ORDER BY c.nombre ASC")
    List<DTOFiltro> obtenerFiltroCultivos();

    boolean existsByIdAndEstablecimientoId(UUID idActividad, UUID establecimientoId);

    @Query("select new com.mza_agrotours.backend.dtos.administrador_sistemas.ConteoPorEstablecimientoDTO(a.establecimiento.id, count(a)) " +
            "from Actividad a " +
            "where a.establecimiento.id in :ids " +
            "and a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO " +
            "group by a.establecimiento.id")
    List<ConteoPorEstablecimientoDTO> countPublicadasByEstablecimientoIds(@Param("ids") Set<UUID> establecimientoIds);

    @Query("SELECT a FROM Actividad a " +
            "WHERE a.id = :actividadId " +
            "AND a.fechaHoraBaja IS NULL " +
            "AND a.estado.nombre = com.mza_agrotours.backend.enums.EstadoActividadNombre.PUBLICADO " +
            "AND a.establecimiento.estadoActual.estadoEstablecimiento.nombre = com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre.ACTIVO")
    Optional<Actividad> findByIdVigenteConEstablecimientoActivo(@Param("actividadId") UUID actividadId);
}
