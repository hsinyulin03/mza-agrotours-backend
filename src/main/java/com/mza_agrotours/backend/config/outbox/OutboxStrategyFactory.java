package com.mza_agrotours.backend.config.outbox;

import com.mza_agrotours.backend.entities.Outbox;
import org.springframework.stereotype.Component;

@Component
public class OutboxStrategyFactory {
    private final EliminarUsuarioFirebaseOutboxStrategy eliminarUsuarioFirebaseOutboxStrategy;
    private final AddMiembroEstablecimientoOutboxStrategy addMiembroEstablecimientoOutboxStrategy;
    private final DelMiembroEstablecimientoOutboxStrategy delMiembroEstablecimientoOutboxStrategy;


    public OutboxStrategyFactory(EliminarUsuarioFirebaseOutboxStrategy eliminarUsuarioFirebaseOutboxStrategy,
                                 AddMiembroEstablecimientoOutboxStrategy addMiembroEstablecimientoOutboxStrategy,
                                 DelMiembroEstablecimientoOutboxStrategy delMiembroEstablecimientoOutboxStrategy) {
        this.eliminarUsuarioFirebaseOutboxStrategy = eliminarUsuarioFirebaseOutboxStrategy;
        this.addMiembroEstablecimientoOutboxStrategy = addMiembroEstablecimientoOutboxStrategy;
        this.delMiembroEstablecimientoOutboxStrategy = delMiembroEstablecimientoOutboxStrategy;
    }

    public OutboxStrategy getStrategy(Outbox outbox) {
        return switch (outbox.getOperacion()) {
            case ELIMINAR_USUARIO -> eliminarUsuarioFirebaseOutboxStrategy;
            case AGREGAR_MIEMBRO_ESTABLECIMIENTO -> addMiembroEstablecimientoOutboxStrategy;
            case QUITAR_MIEMBRO_ESTABLECIMIENTO -> delMiembroEstablecimientoOutboxStrategy;
        };
    }
}
