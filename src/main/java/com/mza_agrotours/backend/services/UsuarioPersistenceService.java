package com.mza_agrotours.backend.services;

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
    private final SolicitudEstablecimientoService solicitudEstablecimientoService;

    public UsuarioPersistenceService(UsuarioRepository usuarioRepository, VisitanteRepository visitanteRepository, SolicitudEstablecimientoService solicitudEstablecimientoService) {
        this.usuarioRepository = usuarioRepository;
        this.visitanteRepository = visitanteRepository;
        this.solicitudEstablecimientoService = solicitudEstablecimientoService;
    }

    @Transactional
    public Usuario saveUsuarioConVisitante(Usuario usuario, Visitante visitante) {
        Usuario savedUsuario = this.usuarioRepository.save(usuario);
        this.visitanteRepository.save(visitante);
        return savedUsuario;
    }

    @Transactional
    public void softDeleteUsuario(Usuario usuario) {
        usuario.setFechaHoraBaja(LocalDateTime.now());
        this.usuarioRepository.save(usuario);
        // Las solicitudes pendientes del usuario dado de baja ya no pueden evaluarse y se cambian a estado rechazado
        this.solicitudEstablecimientoService.rechazarSolicitudesPendientesPorBajaUsuario(usuario);
    }
}
