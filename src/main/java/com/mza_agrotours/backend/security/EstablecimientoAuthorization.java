package com.mza_agrotours.backend.security;

import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.ProductorRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component("estAuth")
public class EstablecimientoAuthorization {
    private final ProductorRepository productorRepository;
    private final EstablecimientoRepository establecimientoRepository;

    public EstablecimientoAuthorization(ProductorRepository productorRepository,
                                        EstablecimientoRepository establecimientoRepository) {
        this.productorRepository = productorRepository;
        this.establecimientoRepository = establecimientoRepository;
    }

    @Transactional(readOnly = true)
    public boolean tienePermiso(Authentication authentication, UUID establecimientoId, String permisoDeseado) {
        // 1. Validamos que haya una sesión activa
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAuthDetails usuario)) {
            return false; // Si no hay token o no es del tipo esperado, se bloquea
        }

        return this.productorRepository.tienePermisoEnEstablecimiento(
                usuario.getEmail(),
                establecimientoId,
                PermisoCodigo.valueOf(permisoDeseado)
        );
    }

    @Transactional(readOnly = true)
    public boolean esTitular(Authentication authentication, UUID establecimientoId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAuthDetails usuario)) {
            return false;
        }
        return this.establecimientoRepository.esTitularVigente(usuario.getEmail(), establecimientoId);
    }

}
