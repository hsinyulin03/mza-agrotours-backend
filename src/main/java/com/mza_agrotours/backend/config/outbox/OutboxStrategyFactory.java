package com.mza_agrotours.backend.config.outbox;

import com.mza_agrotours.backend.entities.Outbox;
import org.springframework.stereotype.Component;

@Component
public class OutboxStrategyFactory {
    private final EliminarUsuarioFirebaseOutboxStrategy eliminarUsuarioFirebaseOutboxStrategy;

    public OutboxStrategyFactory(EliminarUsuarioFirebaseOutboxStrategy eliminarUsuarioFirebaseOutboxStrategy) {
        this.eliminarUsuarioFirebaseOutboxStrategy = eliminarUsuarioFirebaseOutboxStrategy;
    }

    public OutboxStrategy getStrategy(Outbox outbox) {
        return switch (outbox.getOperacion()) {
            case ELIMINAR_USUARIO -> eliminarUsuarioFirebaseOutboxStrategy;
            default -> null;
        };
    }
}
