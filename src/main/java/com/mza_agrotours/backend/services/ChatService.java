package com.mza_agrotours.backend.services;

import com.google.firebase.database.*;
import com.mza_agrotours.backend.dtos.chat.ChatEstablecimientoDTO;
import com.mza_agrotours.backend.dtos.chat.ChatSnapshotDTO;
import com.mza_agrotours.backend.dtos.chat.ChatUsuarioDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.ChatError;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ChatService {
    private final EstablecimientoRepository establecimientoRepository;
    private final UsuarioRepository usuarioRepository;

    private final FirebaseDatabase firebaseDatabase;

    public ChatService(EstablecimientoRepository establecimientoRepository, UsuarioRepository usuarioRepository, FirebaseDatabase firebaseDatabase) {
        this.establecimientoRepository = establecimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.firebaseDatabase = firebaseDatabase;
    }

    public void iniciarChat(UUID establecimientoId, String usuarioEmail) {
        Establecimiento establecimiento = establecimientoRepository.findByIdAndFechaHoraBajaIsNull(establecimientoId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el establecimiento con el ID: " + establecimientoId));

        Usuario usuario = this.usuarioRepository.findActiveByEmail(usuarioEmail)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario"));

        String usuarioId = usuario.getFirebaseUID();
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ValidacionNegocioException("El usuario no tiene una cuenta de Firebase asociada");
        }

        String chatId = usuarioId + "_" + establecimientoId;
        DatabaseReference chatRef = firebaseDatabase.getReference("chats").child(chatId);

        CompletableFuture<Boolean> commitFuture = new CompletableFuture<Boolean>();
        chatRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() != null) {
                    return Transaction.abort();
                }

                currentData.setValue(new ChatSnapshotDTO(usuarioId, establecimientoId.toString(), System.currentTimeMillis()));
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    commitFuture.completeExceptionally(error.toException());
                    return;
                }

                commitFuture.complete(committed);
            }
        });

        boolean existeChat;
        try {
            existeChat = !commitFuture.get(5000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ee) {
            throw new RuntimeException("Fallo al verificar la existencia del chat", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fallo al verificar la existencia del chat", ie);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout al verificar la existencia del chat");
        }

        if (existeChat) {
            throw new AppException(ChatError.CHAT_YA_EXISTE);
        }

        crearNuevoChat(establecimiento, usuario, usuarioId);
    }

    private void crearNuevoChat(Establecimiento establecimiento, Usuario usuario, String usuarioId) {
        final DatabaseReference rootRef = firebaseDatabase.getReference("");

        UUID establecimientoId = establecimiento.getId();
        String nuevoChatId = usuarioId + "_" + establecimientoId;

        long ahora = System.currentTimeMillis();

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("/chats_usuario/" + usuarioId + "/" + nuevoChatId, new ChatUsuarioDTO(establecimientoId.toString(), establecimiento.getNombre(), null, ahora, 0));
        chatData.put("/chats_establecimiento/" + establecimientoId + "/" + nuevoChatId, new ChatEstablecimientoDTO(usuarioId, usuario.getNombre(), null, ahora, 0));

        try {
            rootRef.updateChildrenAsync(chatData).get(5000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ee) {
            throw new RuntimeException("Fallo al crear el chat", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fallo al crear el chat", ie);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout al crear el chat");
        }
    }

    public void agregarMiembroAEstablecimiento(UUID establecimientoId, UUID usuarioId) {
        DatabaseReference miembrosRef = firebaseDatabase.getReference("establecimiento_miembro/" + establecimientoId + "/" + usuarioId);

        try {
            miembrosRef.setValueAsync(true).get(5000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ee) {
            throw new RuntimeException("Fallo al añadir al miembro", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fallo al añadir al miembro", ie);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout al añadir al miembro expirado");
        }
    }

    public void quitarMiembroDelEstablecimiento(UUID establecimientoId, UUID usuarioId) {
        DatabaseReference miembrosRef = firebaseDatabase.getReference("establecimiento_miembro/" + establecimientoId + "/" + usuarioId);

        try {
            miembrosRef.removeValueAsync().get(5000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ee) {
            throw new RuntimeException("Fallo al quitar al miembro", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fallo al quitar al miembro", ie);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout al quitar al miembro expirado");
        }
    }

    public void quitarTodosLosMiembrosDeEstablecimiento(UUID establecimientoId) {
        DatabaseReference miembrosRef = firebaseDatabase.getReference("establecimiento_miembro/" + establecimientoId);

        try {
            miembrosRef.removeValueAsync().get(5000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ee) {
            throw new RuntimeException("Fallo al quitar a los miembros", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fallo al quitar a los miembros", ie);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout al quitar a los miembros expirado");
        }
    }
}
