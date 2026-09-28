package com.mza_agrotours.backend.config.outbox;

import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.services.ChatService;
import org.springframework.stereotype.Component;

@Component
public class AddMiembroEstablecimientoOutboxStrategy implements OutboxStrategy {
    private final ChatService chatService;

    public AddMiembroEstablecimientoOutboxStrategy(ChatService chatService) {
        this.chatService = chatService;
    }

    public void resolver(Outbox outbox) throws Exception {
        this.chatService.agregarMiembroAEstablecimiento(outbox.getId().toString());
    }
}
