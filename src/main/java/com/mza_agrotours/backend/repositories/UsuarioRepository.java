package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    @Query("select u from Usuario u where lower(u.email) = lower(:email) and u.fechaHoraBaja is null")
    Optional<Usuario> findActiveByEmail(@Param("email") String email);
}
