package com.mza_agrotours.backend.config.outbox;

import com.mza_agrotours.backend.entities.Outbox;
import org.springframework.stereotype.Component;

@Component
public class OutboxStrategyFactory {
    private final EliminarUsuarioFirebaseOutboxStrategy eliminarUsuarioFirebaseOutboxStrategy;
    private final AddMiembroEstablecimientoOutboxStrategy addMiembroEstablecimientoOutboxStrategy;
    private final DelMiembroEstablecimientoOutboxStrategy delMiembroEstablecimientoOutboxStrategy;
    private final DelEstablecimientoOutboxStrategy delEstablecimientoOutboxStrategy;
    private final DelUsuarioChatFirebaseOutboxStrategy delUsuarioChatFirebaseOutboxStrategy;
    private final DelActividadChatOutboxStrategy delActividadChatOutboxStrategy;

    public OutboxStrategyFactory(EliminarUsuarioFirebaseOutboxStrategy eliminarUsuarioFirebaseOutboxStrategy,
                                 AddMiembroEstablecimientoOutboxStrategy addMiembroEstablecimientoOutboxStrategy,
                                 DelMiembroEstablecimientoOutboxStrategy delMiembroEstablecimientoOutboxStrategy,
                                 DelEstablecimientoOutboxStrategy delEstablecimientoOutboxStrategy,
                                 DelUsuarioChatFirebaseOutboxStrategy delUsuarioChatFirebaseOutboxStrategy,
                                 DelActividadChatOutboxStrategy delActividadChatOutboxStrategy) {
        this.eliminarUsuarioFirebaseOutboxStrategy = eliminarUsuarioFirebaseOutboxStrategy;
        this.addMiembroEstablecimientoOutboxStrategy = addMiembroEstablecimientoOutboxStrategy;
        this.delMiembroEstablecimientoOutboxStrategy = delMiembroEstablecimientoOutboxStrategy;
        this.delEstablecimientoOutboxStrategy = delEstablecimientoOutboxStrategy;
        this.delUsuarioChatFirebaseOutboxStrategy = delUsuarioChatFirebaseOutboxStrategy;
        this.delActividadChatOutboxStrategy = delActividadChatOutboxStrategy;
    }

    public OutboxStrategy getStrategy(Outbox outbox) {
        return switch (outbox.getOperacion()) {
            case ELIMINAR_USUARIO -> eliminarUsuarioFirebaseOutboxStrategy;
            case AGREGAR_MIEMBRO_ESTABLECIMIENTO -> addMiembroEstablecimientoOutboxStrategy;
            case QUITAR_MIEMBRO_ESTABLECIMIENTO -> delMiembroEstablecimientoOutboxStrategy;
            case QUITAR_ESTABLECIMIENTO -> delEstablecimientoOutboxStrategy;
            case QUITAR_USUARIO_CHAT -> delUsuarioChatFirebaseOutboxStrategy;
            case QUITAR_ACTIVIDAD_CHAT -> delActividadChatOutboxStrategy;
        };
    }
}
