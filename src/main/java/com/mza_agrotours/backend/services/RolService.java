package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.enums.PermisoNombre;
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

    public List<PermisoNombre> obtenerPermisosAdminPorEmail(String email) {
        return this.administradorSistemasRepository.findPermisoNombresByEmailActivo(email);
    }
}
