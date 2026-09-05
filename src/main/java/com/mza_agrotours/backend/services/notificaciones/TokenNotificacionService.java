package com.mza_agrotours.backend.services.notificaciones;

import com.mza_agrotours.backend.dtos.notificacion.TokenNotificacionResponseDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.notificacion.TokenNotificacion;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.mappers.TokenNotificacionMapper;
import com.mza_agrotours.backend.repositories.TokenNotificacionRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenNotificacionService {

    private final TokenNotificacionRepository tokenNotificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final TokenNotificacionMapper tokenNotificacionMapper;

    public TokenNotificacionService(TokenNotificacionRepository tokenNotificacionRepository,
                                    UsuarioRepository usuarioRepository,
                                    TokenNotificacionMapper tokenNotificacionMapper) {
        this.tokenNotificacionRepository = tokenNotificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.tokenNotificacionMapper = tokenNotificacionMapper;
    }

    @Transactional
    public TokenNotificacionResponseDTO registrarToken(String emailUsuario, String token) {
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
        return this.tokenNotificacionMapper.tokenNotificacionToTokenNotificacionResponseDTO(tokenNotificacion);
    }

    @Transactional
    public TokenNotificacionResponseDTO darBajaToken(String emailUsuario, String token) {
        Usuario usuario = obtenerUsuario(emailUsuario);

        // impide que A de de baja el token que FCM ya reasigno a B en el mismo dispositivo.
        Optional<TokenNotificacion> propio = this.tokenNotificacionRepository
                .findByToken(token)
                .filter(t -> t.getUsuario().getId().equals(usuario.getId()));

        // No existe o no es mio: No hay nada que dar de baja
        if (propio.isEmpty()) {
            return null;
        }
        TokenNotificacion tokenNotificacion = propio.get();

        if (tokenNotificacion.getFechaHoraBaja() == null) {
            tokenNotificacion.setFechaHoraBaja(LocalDateTime.now());
            this.tokenNotificacionRepository.save(tokenNotificacion);
        }

        return this.tokenNotificacionMapper.tokenNotificacionToTokenNotificacionResponseDTO(tokenNotificacion);
    }

    private Usuario obtenerUsuario(String email) {
        return this.usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UsuarioNotFound(email));
    }
}
