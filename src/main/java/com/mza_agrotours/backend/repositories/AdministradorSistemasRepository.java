package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.PermisoNombre;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdministradorSistemasRepository extends BaseEntityRepository<AdministradorSistemas, UUID> {
    Optional<AdministradorSistemas> findByUsuarioAndFechaHoraBajaIsNull(Usuario usuario);

    Optional<AdministradorSistemas> findByIdAndFechaHoraBajaIsNull(UUID id);

    boolean existsByUsuarioAndFechaHoraBajaIsNull(Usuario usuario);

    List<AdministradorSistemas> findByFechaHoraBajaIsNull();

    @Query("SELECT p.nombre FROM AdministradorSistemas a JOIN a.rol r JOIN r.permisos p WHERE a.usuario.email = :email AND a.fechaHoraBaja IS NULL")
    List<PermisoNombre> findPermisoNombresByEmailActivo(@Param("email") String email);

    Integer countByRolAndFechaHoraBajaIsNull(Rol rol);
}
