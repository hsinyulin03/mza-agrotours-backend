package com.mza_agrotours.backend.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.mza_agrotours.backend.dtos.UsuarioCreateReq;
import com.mza_agrotours.backend.dtos.UsuarioGetDTO;
import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.TipoIdentificacionNombre;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.exceptions.TipoIdentificacionInvalidoException;
import com.mza_agrotours.backend.exceptions.UsuarioAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.mappers.UsuarioMapper;
import com.mza_agrotours.backend.repositories.TipoIdentificacionRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          TipoIdentificacionRepository tipoIdentificacionRepository,
                          UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
        this.usuarioMapper = usuarioMapper;
    }

    public UsuarioGetDTO createUsuario(UsuarioCreateReq usuarioCreateReq) throws Exception {
        UserRecord record = null;

        try {
            List<Usuario> usuariosConEmail = usuarioRepository.findByEmailAndFechaHoraBajaIsNull(usuarioCreateReq.getEmail());

            if (!usuariosConEmail.isEmpty()) {
                throw new UsuarioAlreadyExistsException("Ya existe un usuario con ese email");
            }

            TipoIdentificacion tipoIdentificacion = resolveTipoIdentificacion(usuarioCreateReq.getTipoIdentificacion());

            record = FirebaseAuth.getInstance().createUser(usuarioMapper.usuarioCreateReqToFirebaseCreateRequest(usuarioCreateReq));

            Usuario nuevoUsuario = usuarioMapper.usuarioCreateReqToUsuario(usuarioCreateReq);
            nuevoUsuario.setFirebaseUID(record.getUid());
            nuevoUsuario.setTipoIdentificacion(tipoIdentificacion);
            Usuario usuario = usuarioRepository.saveAndFlush(nuevoUsuario);

            return usuarioMapper.usuarioToUsuarioGetDTO(usuario);
        } catch (Exception e) {
            if (record != null) {
                try {
                    FirebaseAuth.getInstance().deleteUser(record.getUid());
                } catch (FirebaseAuthException firebaseAuthException) {
                    // TODO: hacer un script que elimine los usuarios que no se han podido crear
                    log.error(
                            "USUARIO HUERFANO: se creo el usuario en Firebase con UID={} y email={} " +
                                    "pero fallo el guardado en la base de datos Y ADEMAS fallo el borrado " +
                                    "compensatorio en Firebase. Requiere limpieza manual/reconciliacion.",
                            record.getUid(),
                            usuarioCreateReq.getEmail(),
                            firebaseAuthException
                    );
                }
            }

            throw e;
        }
    }


    private TipoIdentificacion resolveTipoIdentificacion(String nombre) {
        final TipoIdentificacionNombre tipoNombre;
        try {
            tipoNombre = TipoIdentificacionNombre.valueOf(nombre);
        } catch (IllegalArgumentException e) {
            throw new TipoIdentificacionInvalidoException("Tipo de identificacion invalido: " + nombre);
        }

        return tipoIdentificacionRepository.findByNombre(tipoNombre)
                .orElseThrow(() -> new TipoIdentificacionInvalidoException(
                        "Tipo de identificacion no encontrado: " + nombre));
    }

    @Transactional
    public UsuarioGetDTO getUsuarioByEmail(String email) {
        Usuario usuario = usuarioRepository.getByEmail(email);
        if (usuario == null) {
            throw new UsuarioNotFound("Usuario no encontrado");
        }

        return usuarioMapper.usuarioToUsuarioGetDTO(usuario);
    }
}
