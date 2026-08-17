package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolRepository extends BaseEntityRepository<Rol, UUID> {
    boolean existsByNombre(String nombre);

    Optional<Rol> findByNombre(String nombre);

    Optional<Rol> findByIdAndTipoPermiso_NombreAndFechaHoraBajaIsNullAndNombreIsNotContaining(UUID id, TipoPermisoNombre tipoPermiso_nombre, String nombre);

    List<Rol> findByTipoPermiso_NombreAndFechaHoraBajaIsNullAndNombreIsNotContaining(TipoPermisoNombre tipoPermiso_nombre, String nombre);

    Optional<Rol> findByNombreAndFechaHoraBajaIsNull(String nombre);

    @Query("select r from Rol r " +
            "where r.id = :id " +
            "and r.tipoPermiso.nombre = :tipoPermisoNombre " +
            "and r.fechaHoraBaja is null")
    Optional<Rol> find(
            @Param("id") UUID id,
            @Param("tipoPermisoNombre") TipoPermisoNombre tipoPermisoNombre
    );
    @Query("select r from Rol r " +
            "where r.id = :id " +
            "and r.tipoPermiso.nombre = :tipoPermisoNombre " +
            "and r.nombre <> :nombreExcluido " +
            "and ((:estId is null and r.establecimiento is null) " +
            "       or r.establecimiento.id = :estId) " +
            "and r.fechaHoraBaja is null ")
    Optional<Rol> findVigenteByIdScoped(UUID id, TipoPermisoNombre tipoPermisoNombre, String nombreExcluido, UUID estId);

    @Query("select r from Rol r " +
            "where r.tipoPermiso.nombre = :tipo " +
            "and r.fechaHoraBaja is null " +
            "and ((:estId is null and r.establecimiento is null) " +
            "     or r.establecimiento.id = :estId)")
    List<Rol> findVigentesEnScope(@Param("tipo") TipoPermisoNombre tipo,
                                  @Param("estId") UUID estId);

    @Query("SELECT COUNT(r) > 0 FROM Rol r WHERE r.nombre = :nombre " +
            "AND r.tipoPermiso.nombre = :tipo "+
            "AND (r.establecimiento.id = :estId OR (:estId IS NULL AND r.establecimiento IS NULL))  "+
            "AND r.fechaHoraBaja IS NULL")
    boolean existsByNombreAndTipoPermisoAndEstablecimiento(
            @Param("nombre") String nombre,
            @Param("tipo") TipoPermisoNombre tipo,
            @Param("estId") UUID estId);
}

