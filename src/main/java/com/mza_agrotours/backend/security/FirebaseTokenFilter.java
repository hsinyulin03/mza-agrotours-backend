package com.mza_agrotours.backend.security;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.services.RolService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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
                var authorities = getAuthorities(decodedToken.getEmail());

                UsuarioAuthDetails usuarioAuthDetails = UsuarioAuthDetails.builder()
                        .email(decodedToken.getEmail())
                        .build();

                var authentication = new UsernamePasswordAuthenticationToken(usuarioAuthDetails, null, authorities);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (FirebaseAuthException e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> getAuthorities(String email) {
        try {
            List<Permiso> permisosEmail = this.rolService.obtenerPermisosAdminPorEmail(email);
            return permisosEmail
                    .stream()
                    .map((p) -> new SimpleGrantedAuthority(p.getNombre().name()))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
