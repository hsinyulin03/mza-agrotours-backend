package com.mza_agrotours.backend.services;

import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FirebaseService {
    private static final Logger log = LoggerFactory.getLogger(FirebaseService.class);

    private final UsuarioRepository usuarioRepository;

    public FirebaseService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void eliminarUsuarioDeFirebase(Outbox outboxEliminar) throws FirebaseAuthException {
        Usuario usuario = this.usuarioRepository.findById(UUID.fromString(outboxEliminar.getEntidadId()))
                .orElse(null);

        if (usuario == null) {
            return;
        }

        try {
            FirebaseAuth.getInstance().deleteUser(usuario.getFirebaseUID());
        } catch (FirebaseAuthException fae) {
            if (fae.getAuthErrorCode().equals(AuthErrorCode.USER_NOT_FOUND)) {
                return;
            }

            log.error(
                    "USUARIO INCONSISTENTE: se elimino el usuario en la base de datos con UID={} y email={} " +
                            "pero fallo la baja en Firebase. ",
                    usuario.getFirebaseUID(),
                    usuario.getEmail()
            );
            throw fae;
        }
    }
}
