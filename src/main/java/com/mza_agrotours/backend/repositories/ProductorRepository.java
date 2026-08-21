package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductorRepository extends BaseEntityRepository<Productor, UUID> {

    Integer countByRolAndFechaHoraBajaIsNull(Rol rol);

    @Query("SELECT COUNT(pr) > 0 FROM Productor pr " +
            "JOIN pr.rol r JOIN r.permisos p " +
            "WHERE pr.usuario.email = :email " +
            "AND pr.establecimiento.id = :establecimientoId " +
            "AND pr.establecimiento.fechaHoraBaja IS NULL " +
            "AND pr.fechaHoraBaja IS NULL " +
            "AND r.fechaHoraBaja IS NULL " +
            "AND p.codigo = :permiso " +
            "AND pr.estadoActual.nombre = 'ACTIVO'")
    boolean tienePermisoEnEstablecimiento(
            @Param("email") String email,
            @Param("establecimientoId") UUID establecimientoId,
            @Param("permiso") PermisoCodigo permiso);
    List<Productor> findByUsuarioAndFechaHoraBajaIsNull(Usuario usuario);
}