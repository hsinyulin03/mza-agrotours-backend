package com.mza_agrotours.backend.services.notificaciones;

import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.notificacion.TokenNotificacion;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.repositories.TokenNotificacionRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenNotificacionService {

    private final TokenNotificacionRepository tokenNotificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public TokenNotificacionService(TokenNotificacionRepository tokenNotificacionRepository,
                                    UsuarioRepository usuarioRepository) {
        this.tokenNotificacionRepository = tokenNotificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrarToken(String emailUsuario, String token) {
        Usuario usuario = obtenerUsuario(emailUsuario);
        LocalDateTime ahora = LocalDateTime.now();

        TokenNotificacion tokenNotificacion = this.tokenNotificacionRepository
                .findByToken(token)
                .orElseGet(() -> {
                    TokenNotificacion nuevo = new TokenNotificacion();
                    nuevo.setToken(token);
                    nuevo.setFechaHoraAlta(ahora);
                    return nuevo;
                });

        // Reasignacion deliberada: el token identifica al DISPOSITIVO, no a la persona.
        // Si A cierra sesion y B entra en el mismo celular, FCM devuelve el mismo token.
        // Sin esta linea, B recibiria las notificaciones de A.
        tokenNotificacion.setUsuario(usuario);
        tokenNotificacion.setFechaHoraUltimoUso(ahora);

        // Reactiva un token dado de baja: sin esto el insert chocaria contra el unique.
        tokenNotificacion.setFechaHoraBaja(null);
        this.tokenNotificacionRepository.save(tokenNotificacion);
    }

    @Transactional
    public void eliminarToken(String emailUsuario, String token) {
        Usuario usuario = obtenerUsuario(emailUsuario);

        this.tokenNotificacionRepository.findByToken(token)
                .filter(t -> t.getUsuario().getId().equals(usuario.getId()))
                .ifPresent(t -> {
                    t.setFechaHoraBaja(LocalDateTime.now());
                    this.tokenNotificacionRepository.save(t);
                });
    }

    private Usuario obtenerUsuario(String email) {
        return this.usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UsuarioNotFound(email));
    }
}
