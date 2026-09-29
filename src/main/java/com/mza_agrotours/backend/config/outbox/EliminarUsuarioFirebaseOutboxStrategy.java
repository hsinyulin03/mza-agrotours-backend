package com.mza_agrotours.backend.config.outbox;

import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.services.FirebaseService;
import org.springframework.stereotype.Component;

@Component
public class EliminarUsuarioFirebaseOutboxStrategy implements OutboxStrategy {
    private final FirebaseService firebaseService;

    public EliminarUsuarioFirebaseOutboxStrategy(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @Override
    public void resolver(Outbox outbox) throws Exception{
        this.firebaseService.eliminarUsuarioDeFirebase(outbox);
    }
}
