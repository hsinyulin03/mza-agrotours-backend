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

    List<Rol> findByTipoPermiso_NombreAndFechaHoraBajaIsNull(TipoPermisoNombre tipoPermisoNombre);

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
            "and (:establecimientoId is null and r.establecimiento.id is null or r.establecimiento.id = :establecimientoId) " +
            "and r.esProtegido = false " +
            "and r.fechaHoraBaja is null")
    Optional<Rol> findVigenteMutableByIdScoped(UUID id, TipoPermisoNombre tipoPermisoNombre, UUID establecimientoId);

    @Query("select count(r) > 0 " +
            "from Rol r " +
            "where r.tipoPermiso.nombre = :tipoPermisoNombre " +
            "and r.nombre = :nombre " +
            "and (:establecimientoId is null and r.establecimiento.id is null or r.establecimiento.id = :establecimientoId) " +
            "and r.fechaHoraBaja is null")
    boolean existsByNombreScoped(String nombre, TipoPermisoNombre tipoPermisoNombre, UUID establecimientoId);
}

