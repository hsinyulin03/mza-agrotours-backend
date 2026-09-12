package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.PermisoCodigo;
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

    @Query("SELECT p.codigo FROM AdministradorSistemas a JOIN a.rol r JOIN r.permisos p WHERE a.usuario.email = :email AND a.fechaHoraBaja IS NULL")
    List<PermisoCodigo> findPermisoCodigosByEmailActivo(@Param("email") String email);

    @Query("SELECT a FROM AdministradorSistemas a WHERE a.usuario.email = :email AND a.fechaHoraBaja IS NULL")
    Optional<AdministradorSistemas> findByEmailActivo(@Param("email") String email);

    Integer countByRolAndFechaHoraBajaIsNull(Rol rol);
}
