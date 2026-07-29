package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VisitanteRepository extends JpaRepository<Visitante, UUID> {
    Optional<Visitante> findByUsuario(Usuario usuario);
}
