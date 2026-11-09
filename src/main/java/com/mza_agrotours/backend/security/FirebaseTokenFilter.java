package com.mza_agrotours.backend.security;


import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.services.roles_permisos.AdminAuthoritiesDTO;
import com.mza_agrotours.backend.services.roles_permisos.RolService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {
    private final RolService rolService;

    public FirebaseTokenFilter(RolService rolService) {
        this.rolService = rolService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                // TODO: split on two filters AdminAuthoritiesFilter and ProductoresAuthoritiesFilter or something
                var authorities = getAdminAuthorities(decodedToken.getEmail());

                UsuarioAuthDetails usuarioAuthDetails = UsuarioAuthDetails.builder()
                        .email(decodedToken.getEmail())
                        .firebaseUID(decodedToken.getUid())
                        .build();

                var authentication = new UsernamePasswordAuthenticationToken(usuarioAuthDetails, null, authorities);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (FirebaseAuthException e) {
                SecurityContextHolder.clearContext();
                AuthErrorCode codigo = e.getAuthErrorCode();
                if (codigo == null || codigo == AuthErrorCode.CERTIFICATE_FETCH_FAILED)
                    log.error("No se pudo validar el token con Firebase Authentication", e);
                else
                    log.debug("Token de Firebase rechazado: {}", codigo);
            }
        }
        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> getAdminAuthorities(String email) {
        AdminAuthoritiesDTO adminAuthorities = this.rolService.obtenerRolAdminVigenteByEmail(email);

        if (adminAuthorities == null) {
            return List.of();
        }

        // Autoridades de permisos
        List<SimpleGrantedAuthority> authorities = new ArrayList<>(adminAuthorities.permisos()
                .stream().map(SimpleGrantedAuthority::new)
                .toList());

        //Autoridad de rol
        authorities.add(new SimpleGrantedAuthority("ROLE_" + adminAuthorities.rolNombre()));

        return authorities;
    }
}
