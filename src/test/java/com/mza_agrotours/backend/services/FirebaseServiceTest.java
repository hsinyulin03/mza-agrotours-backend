package com.mza_agrotours.backend.services;

import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * La baja en Firebase tiene que ser idempotente: el outbox puede reintentarla despues de
 * un primer intento que si llego a borrar, y ese reintento no puede contar como fallo.
 */
class FirebaseServiceTest {

    private static final String FIREBASE_UID = "uid-test";

    private UsuarioRepository usuarioRepository;
    private FirebaseAuth firebaseAuth;
    private MockedStatic<FirebaseAuth> firebaseAuthStatic;
    private FirebaseService firebaseService;

    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        this.usuarioRepository = mock(UsuarioRepository.class);
        this.firebaseAuth = mock(FirebaseAuth.class);
        this.firebaseAuthStatic = mockStatic(FirebaseAuth.class);
        firebaseAuthStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);

        this.firebaseService = new FirebaseService(usuarioRepository);

        this.usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setFirebaseUID(FIREBASE_UID);
        usuario.setEmail("baja@test.local");
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
    }

    @AfterEach
    void tearDown() {
        firebaseAuthStatic.close();
    }

    @Test
    void cuandoFirebaseBorraAlUsuario_entoncesNoFalla() throws Exception {
        assertThatCode(() -> firebaseService.eliminarUsuarioDeFirebase(outboxDe(usuarioId)))
                .doesNotThrowAnyException();

        verify(firebaseAuth).deleteUser(FIREBASE_UID);
    }

    @Test
    void dadoQueFirebaseYaNoTieneAlUsuario_cuandoSeReintenta_entoncesCuentaComoExito() throws Exception {
        doThrow(authException(AuthErrorCode.USER_NOT_FOUND)).when(firebaseAuth).deleteUser(FIREBASE_UID);

        assertThatCode(() -> firebaseService.eliminarUsuarioDeFirebase(outboxDe(usuarioId)))
                .doesNotThrowAnyException();
    }

    @Test
    void dadoOtroErrorDeFirebase_cuandoSeElimina_entoncesPropagaLaExcepcion() throws Exception {
        FirebaseAuthException error = authException(AuthErrorCode.CONFIGURATION_NOT_FOUND);
        doThrow(error).when(firebaseAuth).deleteUser(FIREBASE_UID);

        assertThatThrownBy(() -> firebaseService.eliminarUsuarioDeFirebase(outboxDe(usuarioId)))
                .isSameAs(error);
    }

    @Test
    void dadoQueElUsuarioNoEstaEnLaBase_cuandoSeElimina_entoncesNoLlamaAFirebase() throws Exception {
        UUID inexistente = UUID.randomUUID();
        when(usuarioRepository.findById(inexistente)).thenReturn(Optional.empty());

        assertThatCode(() -> firebaseService.eliminarUsuarioDeFirebase(outboxDe(inexistente)))
                .doesNotThrowAnyException();

        verify(firebaseAuth, never()).deleteUser(any());
    }

    private static Outbox outboxDe(UUID usuarioId) {
        Outbox outbox = new Outbox();
        outbox.setEntidadId(usuarioId.toString());
        return outbox;
    }

    private static FirebaseAuthException authException(AuthErrorCode codigo) {
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getAuthErrorCode()).thenReturn(codigo);
        return exception;
    }
}
