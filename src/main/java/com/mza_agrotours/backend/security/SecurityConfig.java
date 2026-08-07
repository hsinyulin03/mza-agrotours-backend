package com.mza_agrotours.backend.security;

import com.mza_agrotours.backend.enums.PermisoNombre;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final FirebaseTokenFilter firebaseTokenFilter;

    public SecurityConfig(FirebaseTokenFilter firebaseTokenFilter) {
        this.firebaseTokenFilter = firebaseTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("*"));
                    config.setAllowedMethods(List.of("*"));
                    config.setAllowedHeaders(List.of("*"));
                    return config;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Administradores
                        .requestMatchers("/administradores-sistemas/").hasAuthority(PermisoNombre.LEER_ADMIN.name())
                        .requestMatchers("/administradores-sistemas/**").hasAuthority(PermisoNombre.GESTIONAR_ADMIN.name())

                        // Usuario
                        .requestMatchers("/usuario/**").authenticated()

                        // Solicitudes
                        .requestMatchers("/solicitudes-establecimiento/me/**").authenticated()
                        .requestMatchers("/solicitudes-establecimiento/").hasAuthority(PermisoNombre.LEER_SOLICITUD_ESTABLECIMIENTO.name())
                        .requestMatchers("/solicitudes-establecimiento/observar/**").hasAuthority(PermisoNombre.GESTIONAR_SOLICITUD_ESTABLECIMIENTO.name())
                        .anyRequest().authenticated())
                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

