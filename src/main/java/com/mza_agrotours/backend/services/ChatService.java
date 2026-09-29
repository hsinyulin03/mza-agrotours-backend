package com.mza_agrotours.backend.services;

import com.google.firebase.database.*;
import com.mza_agrotours.backend.dtos.chat.ChatEstablecimientoDTO;
import com.mza_agrotours.backend.dtos.chat.ChatSnapshotDTO;
import com.mza_agrotours.backend.dtos.chat.ChatUsuarioDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.exceptions.*;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.ProductorRepository;
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
    private final ProductorRepository productorRepository;
    private final FirebaseDatabase firebaseDatabase;

    public ChatService(EstablecimientoRepository establecimientoRepository,
                       UsuarioRepository usuarioRepository,
                       ProductorRepository productorRepository,
                       FirebaseDatabase firebaseDatabase) {
        this.establecimientoRepository = establecimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productorRepository = productorRepository;
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

    public void agregarMiembroAEstablecimiento(String productorId) throws FailedFirebaseChatOperationException {
        Productor productor = productorRepository.findByIdAndFechaHoraBajaIsNull(UUID.fromString(productorId))
                .orElseThrow(() -> new AppException(ProductorError.NOT_FOUND));

        UUID establecimientoId = productor.getEstablecimiento().getId();
        String usrFirebaseId = productor.getUsuario().getFirebaseUID();
        DatabaseReference miembrosRef = firebaseDatabase.getReference("establecimiento_miembros/" + establecimientoId + "/" + usrFirebaseId);

        try {
            Map<String, Object> miembroData = new HashMap<>();
            miembroData.put(productorId, true);
            miembrosRef.setValueAsync(miembroData).get(5000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ee) {
            throw new FailedFirebaseChatOperationException("Fallo al añadir al miembro", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new FailedFirebaseChatOperationException("Fallo al añadir al miembro", ie);
        } catch (TimeoutException e) {
            throw new FailedFirebaseChatOperationException("Timeout al añadir al miembro expirado");
        }
    }

    public void quitarMiembroDelEstablecimiento(String productorId) throws FailedFirebaseChatOperationException {
        Productor productor = productorRepository.findById(UUID.fromString(productorId))
                .orElse(null);

        if (productor == null) {
            return;
        }

        String establecimientoId = productor.getEstablecimiento().getId().toString();
        String usrFirebaseId = productor.getUsuario().getFirebaseUID();
        DatabaseReference miembrosRef = firebaseDatabase.getReference("establecimiento_miembros/" + establecimientoId + "/" + usrFirebaseId).child(productorId);

        try {
            miembrosRef.removeValueAsync().get(5000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ee) {
            throw new FailedFirebaseChatOperationException("Fallo al quitar al miembro", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new FailedFirebaseChatOperationException("Fallo al quitar al miembro", ie);
        } catch (TimeoutException e) {
            throw new FailedFirebaseChatOperationException("Timeout al quitar al miembro expirado");
        }
    }

    public void quitarEstablecimiento(String establecimientoId) throws FailedFirebaseChatOperationException {
        DatabaseReference miembrosRef = firebaseDatabase.getReference("establecimiento_miembros/" + establecimientoId);

        try {
            miembrosRef.removeValueAsync().get(5000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ee) {
            throw new FailedFirebaseChatOperationException("Fallo al quitar a los miembros", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new FailedFirebaseChatOperationException("Fallo al quitar a los miembros", ie);
        } catch (TimeoutException e) {
            throw new FailedFirebaseChatOperationException("Timeout al quitar a los miembros expirado");
        }
    }
}
