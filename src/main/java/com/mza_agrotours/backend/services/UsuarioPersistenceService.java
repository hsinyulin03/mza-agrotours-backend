package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.entities.Pais;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import com.mza_agrotours.backend.repositories.VisitanteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UsuarioPersistenceService {
    private final UsuarioRepository usuarioRepository;
    private final VisitanteRepository visitanteRepository;

    public UsuarioPersistenceService(UsuarioRepository usuarioRepository, VisitanteRepository visitanteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.visitanteRepository = visitanteRepository;
    }

    @Transactional
    public Usuario saveUsuarioConVisitante(Usuario usuario, Pais pais) {
        Usuario nuevoUsuario = this.usuarioRepository.save(usuario);

        Visitante visitante = new Visitante();
        visitante.setUsuario(nuevoUsuario);
        visitante.setPais(pais);

        this.visitanteRepository.save(visitante);

        return nuevoUsuario;
    }

    @Transactional
    public Usuario updateUsuarioConVisitante(Usuario usuario, Visitante visitante) {
        Usuario usuarioActualizado = this.usuarioRepository.save(usuario);
        this.visitanteRepository.save(visitante);

        return usuarioActualizado;
    }

    @Transactional
    public void softDeleteUsuario(Usuario usuario) {
        usuario.setFechaHoraBaja(LocalDateTime.now());
        this.usuarioRepository.save(usuario);
    }
}
