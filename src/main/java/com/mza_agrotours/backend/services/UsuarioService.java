package com.mza_agrotours.backend.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.mza_agrotours.backend.dtos.UsuarioCreateReq;
import com.mza_agrotours.backend.dtos.UsuarioGetDTO;
import com.mza_agrotours.backend.dtos.UsuarioUpdateReq;
import com.mza_agrotours.backend.entities.*;
import com.mza_agrotours.backend.exceptions.PaisNotFoundException;
import com.mza_agrotours.backend.exceptions.TipoIdentificacionInvalidoException;
import com.mza_agrotours.backend.exceptions.UsuarioAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.mappers.UsuarioMapper;
import com.mza_agrotours.backend.repositories.PaisRepository;
import com.mza_agrotours.backend.repositories.TipoIdentificacionRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import com.mza_agrotours.backend.repositories.VisitanteRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioPersistenceService usuarioPersistenceService;

    private final UsuarioRepository usuarioRepository;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;
    private final PaisRepository paisRepository;
    private final VisitanteRepository visitanteRepository;

    private final UsuarioMapper usuarioMapper;

    public UsuarioService( UsuarioPersistenceService usuarioPersistenceService,
                        UsuarioRepository usuarioRepository,
                        TipoIdentificacionRepository tipoIdentificacionRepository,
                        VisitanteRepository visitanteRepository,
                        UsuarioMapper usuarioMapper,
                        PaisRepository paisRepository) {
        this.usuarioPersistenceService = usuarioPersistenceService;
        this.usuarioRepository = usuarioRepository;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
        this.visitanteRepository = visitanteRepository;
        this.usuarioMapper = usuarioMapper;
        this.paisRepository = paisRepository;
    }

    public UsuarioGetDTO createUsuario(UsuarioCreateReq usuarioCreateReq) throws Exception {
        UserRecord record = null;
        try {
            usuarioCreateReq.setEmail(usuarioCreateReq.getEmail().trim());

            if (usuarioRepository.findActiveByEmail(usuarioCreateReq.getEmail()).isPresent()) {
                throw new UsuarioAlreadyExistsException("Ya existe un usuario con ese email");
            }

            TipoIdentificacion tipoIdentificacion = resolveTipoIdentificacion(usuarioCreateReq.getTipoIdentificacion());
            Pais pais = this.paisRepository.findByIso2(usuarioCreateReq.getPaisIso2()).orElseThrow(PaisNotFoundException::new);

            record = FirebaseAuth.getInstance().createUser(usuarioMapper.usuarioCreateReqToFirebaseCreateRequest(usuarioCreateReq));

            Usuario nuevoUsuario = usuarioMapper.usuarioCreateReqToUsuario(usuarioCreateReq);
            nuevoUsuario.setFirebaseUID(record.getUid());
            nuevoUsuario.setTipoIdentificacion(tipoIdentificacion);
            Usuario usuario = usuarioPersistenceService.saveUsuarioConVisitante(nuevoUsuario, pais);

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
        String normalizedEmail = email == null ? null : email.trim();
        Usuario usuario = usuarioRepository.findActiveByEmail(normalizedEmail)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        UsuarioGetDTO usuarioGetDTO = usuarioMapper.usuarioToUsuarioGetDTO(usuario);
        Visitante visitante = visitanteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario no tiene un visitante asociado: " + usuario.getId()));
        usuarioGetDTO.setPaisIso2(visitante.getPais().getIso2());

        return usuarioGetDTO;
    }

    @Transactional
    public UsuarioGetDTO updateUsuarioByEmail(String email, UsuarioUpdateReq usuarioUpdateReq) throws Exception {
        Usuario usuario = usuarioRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        if (!usuarioUpdateReq.getEmail().equals(usuario.getEmail()) && usuarioRepository.findActiveByEmail(usuarioUpdateReq.getEmail()).isPresent()) {
            throw new UsuarioAlreadyExistsException("Ya existe un usuario con ese email");
        }

        Visitante visitante = this.visitanteRepository.findByUsuario(usuario).orElseThrow(IllegalStateException::new);
        Pais pais = this.paisRepository.findByIso2(usuarioUpdateReq.getPaisIso2()).orElseThrow(PaisNotFoundException::new);
        TipoIdentificacion tipoIdentificacion = resolveTipoIdentificacion(usuarioUpdateReq.getTipoIdentificacion());

        usuarioMapper.updateUsuarioFromUsuarioUpdateReq(usuario, usuarioUpdateReq);
        usuario.setTipoIdentificacion(tipoIdentificacion);
        visitante.setPais(pais);

        usuario = usuarioRepository.save(usuario);
        visitanteRepository.save(visitante);

        UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(usuario.getFirebaseUID());
        updateRequest.setEmail(usuarioUpdateReq.getEmail());
        updateRequest.setDisplayName(usuarioUpdateReq.getNombre());
        updateRequest.setPhoneNumber(usuarioUpdateReq.getTelefono());

        FirebaseAuth.getInstance().updateUser(updateRequest);

        return usuarioMapper.usuarioToUsuarioGetDTO(usuario);
    }
}
