package com.mza_agrotours.backend.security;

import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.ProductorRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component("estAuth")
public class EstablecimientoAuthorization {
    private final ProductorRepository productorRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final ActividadRepository actividadRepository;

    public EstablecimientoAuthorization(ProductorRepository productorRepository,
                                        EstablecimientoRepository establecimientoRepository,
                                        ActividadRepository actividadRepository) {
        this.productorRepository = productorRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.actividadRepository = actividadRepository;
    }

    @Transactional(readOnly = true)
    public boolean tienePermisoSobreActividad(Authentication authentication, UUID establecimientoId, UUID idActividad, PermisoCodigo permiso) {

        // Verificamos si tiene permiso correspondiente en el establecimiento
        if (!tienePermiso(authentication, establecimientoId, permiso)) {
            return false;
        }

        // Validamos que la actividad realmente pertenezca a ese establecimiento
        return this.actividadRepository.existsByIdAndEstablecimientoId(idActividad, establecimientoId);
    }

    @Transactional(readOnly = true)
    public boolean tienePermiso(Authentication authentication, UUID establecimientoId, PermisoCodigo permiso ) {
        // 1. Validamos que haya una sesión activa
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAuthDetails usuario)) {
            return false; // Si no hay token o no es del tipo esperado, se bloquea
        }

        return this.productorRepository.tienePermisoEnEstablecimiento(
                usuario.getEmail(),
                establecimientoId,
                permiso
        );
    }

    @Transactional(readOnly = true)
    public boolean esTitular(Authentication authentication, UUID establecimientoId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAuthDetails usuario)) {
            return false;
        }
        return this.establecimientoRepository.esTitularVigente(usuario.getEmail(), establecimientoId);
    }

    @Transactional(readOnly = true)
    public boolean esProductorVigente(Authentication authentication, UUID establecimientoId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAuthDetails usuario)) {
            return false;
        }
        return this.productorRepository.esProductorVigenteyActivo(usuario.getEmail(), establecimientoId);
    }


}
