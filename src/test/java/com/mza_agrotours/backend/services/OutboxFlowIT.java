package com.mza_agrotours.backend.services;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.enums.outbox.EstadoOutbox;
import com.mza_agrotours.backend.exceptions.UserDeleteConditionNotMetException;
import com.mza_agrotours.backend.repositories.OutboxRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import com.mza_agrotours.backend.schedules.OutboxScheduler;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * La fila del outbox nace en la misma transaccion que la baja y el primer intento corre en
 * AFTER_COMMIT. Sin @Transactional a proposito: el listener solo se dispara con un commit
 * real, y lo que se verifica es justamente que su REQUIRES_NEW deje el estado persistido.
 * El poller se reemplaza por un mock para que no compita con el evento.
 */
class OutboxFlowIT extends AbstractIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private FixtureUsuario usuarios;

    @MockitoBean
    private FirebaseService firebaseService;

    @MockitoBean
    private OutboxScheduler outboxScheduler;

    private DatabaseReference chatsDelVisitante;

    /**
     * El visitante no tiene chats en Realtime Database, asi que quitarVisitante lee y no escribe nada.
     */
    @BeforeEach
    void visitanteSinChats() {
        DatabaseReference chatsUsuario = mock(DatabaseReference.class);
        chatsDelVisitante = mock(DatabaseReference.class);
        DataSnapshot sinChats = mock(DataSnapshot.class);

        when(firebaseDatabase.getReference("chats_usuario")).thenReturn(chatsUsuario);
        when(chatsUsuario.child(anyString())).thenReturn(chatsDelVisitante);
        doAnswer(invocation -> {
            invocation.<ValueEventListener>getArgument(0).onDataChange(sinChats);
            return null;
        }).when(chatsDelVisitante).addListenerForSingleValueEvent(any());
    }

    @AfterEach
    void limpiarOutbox() {
        outboxRepository.deleteAll();
    }

    @Test
    void dadoQueFirebaseResponde_cuandoSeDaDeBaja_entoncesLaFilaQuedaExitosa() throws Exception {
        Usuario usuario = usuarios.visitante().getUsuario();

        usuarioService.deleteUsuarioByEmail(usuario.getEmail());

        for (Outbox fila : dosFilasDe(usuario)) {
            assertThat(fila.getEstado())
                    .as("el EXITOSO del listener AFTER_COMMIT tiene que llegar a la base")
                    .isEqualTo(EstadoOutbox.EXITOSO);
            assertThat(fila.getReintentos()).isZero();
            assertThat(fila.getFechaHoraProximoIntento()).isAfter(fila.getFechaHoraAlta());
        }

        verify(firebaseService).eliminarUsuarioDeFirebase(any());
        verify(chatsDelVisitante).addListenerForSingleValueEvent(any());
    }

    @Test
    void dadoQueFirebaseFalla_cuandoSeDaDeBaja_entoncesLaBajaSeMantieneYLaFilaQuedaParaReintentar() throws Exception {
        Usuario usuario = usuarios.visitante().getUsuario();
        doThrow(new RuntimeException("Firebase caido")).when(firebaseService).eliminarUsuarioDeFirebase(any());
        doThrow(new RuntimeException("Firebase caido")).when(chatsDelVisitante).addListenerForSingleValueEvent(any());

        usuarioService.deleteUsuarioByEmail(usuario.getEmail());

        assertThat(usuarioRepository.findById(usuario.getId()).orElseThrow().getFechaHoraBaja())
                .as("un fallo de Firebase despues del commit no revierte la baja")
                .isNotNull();

        for (Outbox fila : dosFilasDe(usuario)) {
            assertThat(fila.getEstado()).isEqualTo(EstadoOutbox.PENDIENTE);
            assertThat(fila.getReintentos()).isEqualTo(1);
            assertThat(fila.getFechaHoraProximoIntento()).isAfter(fila.getFechaHoraAlta());
        }
    }

    @Test
    void dadoQueNoCumpleLasCondiciones_cuandoSeIntentaDarDeBaja_entoncesNoSeCreaFilaNiSeLlamaAFirebase() throws Exception {
        Usuario administrador = usuarios.administrador().getUsuario();

        assertThatThrownBy(() -> usuarioService.deleteUsuarioByEmail(administrador.getEmail()))
                .isInstanceOf(UserDeleteConditionNotMetException.class);

        assertThat(filasDe(administrador)).isEmpty();
        verify(firebaseService, never()).eliminarUsuarioDeFirebase(any());
        verifyNoInteractions(firebaseDatabase);
    }

    private List<Outbox> dosFilasDe(Usuario usuario) {
        List<Outbox> filas = filasDe(usuario);
        assertThat(filas).hasSize(2);
        return filas;
    }

    private List<Outbox> filasDe(Usuario usuario) {
        return outboxRepository.findAll().stream()
                .filter(outbox -> outbox.getEntidadId().equals(usuario.getId().toString()))
                .toList();
    }
}
