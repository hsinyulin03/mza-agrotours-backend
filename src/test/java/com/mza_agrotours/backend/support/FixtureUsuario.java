package com.mza_agrotours.backend.support;

import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import com.mza_agrotours.backend.repositories.VisitanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Usuarios y los perfiles que cuelgan directo de uno.
 */
@Component
public class FixtureUsuario {

    @Autowired private FixtureCatalogo catalogo;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private VisitanteRepository visitanteRepository;
    @Autowired private AdministradorSistemasRepository administradorSistemasRepository;

    public Usuario usuario(String prefijoEmail) {
        int n = Seq.next();

        Usuario usuario = new Usuario();
        usuario.setEmail(prefijoEmail + n + "@test.local");
        usuario.setFirebaseUID("uid-" + usuario.getEmail());
        usuario.setNombre("Usuario " + n);
        usuario.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        usuario.setTelefono("2610000000");
        usuario.setFechaHoraAlta(LocalDateTime.now());
        usuario.setIdentificacion(String.format("%08d", n));
        usuario.setTipoIdentificacion(catalogo.tipoIdentificacion());
        return this.usuarioRepository.save(usuario);
    }

    public AdministradorSistemas administrador() {
        AdministradorSistemas administrador = new AdministradorSistemas();
        administrador.setUsuario(usuario("admin"));
        administrador.setRol(catalogo.rolAdmin());
        administrador.setFechaHoraAlta(LocalDateTime.now());
        return this.administradorSistemasRepository.save(administrador);
    }

    public Visitante visitante() {
        Visitante visitante = new Visitante();
        visitante.setUsuario(usuario("visitante"));
        visitante.setPais(catalogo.unPais());
        return this.visitanteRepository.save(visitante);
    }
}
