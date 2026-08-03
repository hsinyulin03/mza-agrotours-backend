package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.exceptions.AdministradorSistemasError;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;
    // TODO: ProductorRepository soon

    public RolService(RolRepository rolRepository, UsuarioRepository usuarioRepository, AdministradorSistemasRepository administradorSistemasRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
    }

    public List<Permiso> obtenerPermisosAdminPorEmail(String email) {
        Usuario usuario = usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        AdministradorSistemas administradorSistemas = administradorSistemasRepository
                .findByUsuarioAndFechaHoraBajaIsNull(usuario)
                .orElseThrow(() -> new AppException(AdministradorSistemasError.NOT_FOUND));

        return administradorSistemas.getRol().getPermisos();
    }
}
