package com.mza_agrotours.backend.services;

import com.google.firebase.database.*;
import com.mza_agrotours.backend.dtos.chat.ChatEstablecimientoDTO;
import com.mza_agrotours.backend.dtos.chat.ChatSnapshotDTO;
import com.mza_agrotours.backend.dtos.chat.ChatUsuarioDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.exceptions.*;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.ProductorRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ChatService {
    private final EstablecimientoRepository establecimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductorRepository productorRepository;
    private final ActividadRepository actividadRepository;
    private final FirebaseDatabase firebaseDatabase;

    public ChatService(EstablecimientoRepository establecimientoRepository,
                       UsuarioRepository usuarioRepository,
                       ProductorRepository productorRepository,
                       ActividadRepository actividadRepository,
                       FirebaseDatabase firebaseDatabase) {
        this.establecimientoRepository = establecimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productorRepository = productorRepository;
        this.actividadRepository = actividadRepository;
        this.firebaseDatabase = firebaseDatabase;
    }

    @Transactional(readOnly = true)
    public void iniciarChat(UUID actividadId, String usuarioEmail) {
        // Busco incluso aquellas cuyo establecimiento esté suspendido
        Actividad actividad = actividadRepository.findByIdAndFechaHoraBajaIsNull(actividadId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la actividad con el ID: " + actividadId));

        Usuario usuario = this.usuarioRepository.findActiveByEmail(usuarioEmail)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario"));

        validarUsuarioPuedeIniciarChat(usuario, actividad);

        String usuarioFirebaseId = usuario.getFirebaseUID();

        String chatId = usuarioFirebaseId + "_" + actividadId;
        DatabaseReference chatRef = firebaseDatabase.getReference("chats").child(chatId);

        CompletableFuture<Boolean> commitFuture = new CompletableFuture<Boolean>();
        chatRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() != null) {
                    return Transaction.abort();
                }

                currentData.setValue(new ChatSnapshotDTO(usuarioFirebaseId, actividad.getEstablecimiento().getId().toString(), System.currentTimeMillis(), false));
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

        crearNuevoChat(actividad, usuario);
    }

    private void crearNuevoChat(Actividad actividad, Usuario usuario) {
        final DatabaseReference rootRef = firebaseDatabase.getReference("");

        String usuarioFirebaseId = usuario.getFirebaseUID();
        String actividadId = actividad.getId().toString();
        String establecimientoId = actividad.getEstablecimiento().getId().toString();
        String nuevoChatId = usuarioFirebaseId + "_" + actividadId;

        long ahora = System.currentTimeMillis();

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("/chats_usuario/" + usuarioFirebaseId + "/" + nuevoChatId, new ChatUsuarioDTO(establecimientoId, actividad.getNombre(), null, ahora, 0));
        chatData.put("/chats_establecimiento/" + actividad.getEstablecimiento().getId() + "/" + nuevoChatId, new ChatEstablecimientoDTO(usuarioFirebaseId, usuario.getNombre(), null, ahora, 0));

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

    public Map<String, String> getNombresChatsByUsuarioAndActividadIds(Usuario usuario, List<UUID> actividadIds) {
        String usuarioId = usuario.getFirebaseUID();
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ValidacionNegocioException("El usuario no tiene una cuenta de Firebase asociada");
        }

        DatabaseReference chatsUsuarioRef = firebaseDatabase.getReference("chats_usuario").child(usuarioId);

        CompletableFuture<Set<String>> chatIdsFuture = new CompletableFuture<>();
        chatsUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Set<String> chatIds = new HashSet<>();
                for (DataSnapshot chat : snapshot.getChildren()) {
                    chatIds.add(chat.getKey());
                }
                chatIdsFuture.complete(chatIds);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                chatIdsFuture.completeExceptionally(error.toException());
            }
        });

        Set<String> chatIds;
        try {
            chatIds = chatIdsFuture.get(5000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ee) {
            throw new RuntimeException("Fallo al obtener los chats del usuario", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fallo al obtener los chats del usuario", ie);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout al obtener los chats del usuario");
        }

        Map<String, String> chatsPorActividad = new HashMap<>();
        for (Actividad actividad : actividadRepository.findAllById(actividadIds)) {
            String chatId = usuarioId + "_" + actividad.getEstablecimiento().getId();
            if (chatIds.contains(chatId)) {
                chatsPorActividad.put(chatId, actividad.getNombre());
            }
        }

        return chatsPorActividad;
    }

    public Map<String, String> getNombresChatsByEstablecimientoAndUsuarioFirebaseIds(Establecimiento establecimiento, List<UUID> usuariosIds) {
        DatabaseReference chatsUsuarioRef = firebaseDatabase.getReference("chats_establecimiento").child(establecimiento.getId().toString());

        CompletableFuture<Set<String>> chatIdsFuture = new CompletableFuture<>();
        chatsUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Set<String> chatIds = new HashSet<>();
                for (DataSnapshot chat : snapshot.getChildren()) {
                    chatIds.add(chat.getKey());
                }
                chatIdsFuture.complete(chatIds);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                chatIdsFuture.completeExceptionally(error.toException());
            }
        });

        Set<String> chatIds;
        try {
            chatIds = chatIdsFuture.get(5000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ee) {
            throw new RuntimeException("Fallo al obtener los chats del usuario", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fallo al obtener los chats del usuario", ie);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout al obtener los chats del usuario");
        }

        Map<String, String> chatsPorActividad = new HashMap<>();
        for (Usuario usuario : usuarioRepository.findAllById(usuariosIds)) {
            String chatId = establecimiento.getId() + "_" + usuario.getFirebaseUID();
            if (chatIds.contains(chatId)) {
                chatsPorActividad.put(chatId, usuario.getNombre());
            }
        }

        return chatsPorActividad;
    }

    private void validarUsuarioPuedeIniciarChat(Usuario usuario, Actividad actividad) {
        if (actividad.getEstado().getNombre() != EstadoActividadNombre.PUBLICADO) {
            throw new ValidacionNegocioException("La actividad no está publicada");
        }
    }
}
