package com.mza_agrotours.backend.security;

import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("adminAuth")
public class AdministradorAuthorization {
    private final AdministradorSistemasRepository administradorSistemasRepository;

    public AdministradorAuthorization(AdministradorSistemasRepository administradorSistemasRepository) {
        this.administradorSistemasRepository = administradorSistemasRepository;
    }

    @Transactional(readOnly = true)
    public boolean esAdministradorVigente(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAuthDetails usuario)) {
            return false;
        }
        return this.administradorSistemasRepository.findByEmailActivo(usuario.getEmail()).isPresent();
    }
}
