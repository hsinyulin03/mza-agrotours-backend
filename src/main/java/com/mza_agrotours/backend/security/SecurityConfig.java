package com.mza_agrotours.backend.security;

import com.mza_agrotours.backend.enums.PermisoCodigo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
public class SecurityConfig {
    private final FirebaseTokenFilter firebaseTokenFilter;

    private static final String ROL_ADMIN_LIDER = "Administrador Líder";

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
                        // TODO: añadir prefijo de ruta /admin por ej /admin/administradores-sistemas
                        // Discusion: el endpoint /admin/administradores-sistemas nos dice el scope (admin)
                        // y el recurso sobre el que se desea leer o modificar (administradores-sistemas)
                        .requestMatchers("/administradores-sistemas/").hasAuthority(PermisoCodigo.LEER_ADMIN.name())
                        .requestMatchers("/administradores-sistemas/**").hasAuthority(PermisoCodigo.GESTIONAR_ADMIN.name())

                        // Roles
                        .requestMatchers(HttpMethod.GET, "/admin/roles").hasAuthority(PermisoCodigo.LEER_ROLES_ADMIN.name())
                        .requestMatchers("/admin/roles").hasRole(ROL_ADMIN_LIDER)
                        .requestMatchers("/admin/roles/**").hasRole(ROL_ADMIN_LIDER)

                        // Pais y departamento
                        .requestMatchers("/pais/**").permitAll()
                        .requestMatchers("/departamentos/**").permitAll()

                        // Usuario
                        .requestMatchers("/usuario/create").permitAll()
                        .requestMatchers("/usuario/**").authenticated()

                        // Solicitudes
                        .requestMatchers("/solicitudes-establecimiento/me/**").authenticated()
                        .requestMatchers("/solicitudes-establecimiento/").hasAuthority(PermisoCodigo.LEER_SOLICITUD_ESTABLECIMIENTO.name())
                        .requestMatchers("/solicitudes-establecimiento/observar/**").hasAuthority(PermisoCodigo.GESTIONAR_SOLICITUD_ESTABLECIMIENTO.name())

                        // Archivos
                        .requestMatchers("/object-storage/**").permitAll()

                        //Permisos
                        .requestMatchers( "/permisos/grupos-permisos/admin").hasAuthority(PermisoCodigo.LEER_ADMIN.name())
                        .requestMatchers("/permisos/grupos-permisos/productor").authenticated()

                        //Actividades
                        .requestMatchers("/actividades/*/reservar").authenticated()
                        .requestMatchers("/actividades/**").permitAll()

                        //Reserva
                        .requestMatchers("/reserva/**").authenticated()

                        //Docs
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()

                        .anyRequest().authenticated())
                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

