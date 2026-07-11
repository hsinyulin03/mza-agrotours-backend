package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    List<Usuario> findByEmailAndFechaHoraBajaIsNull(String email);
    Usuario getByEmail(String email);
}
