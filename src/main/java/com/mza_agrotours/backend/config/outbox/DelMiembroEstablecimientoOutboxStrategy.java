package com.mza_agrotours.backend.config.outbox;

import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.services.ChatService;
import org.springframework.stereotype.Component;

@Component
public class DelMiembroEstablecimientoOutboxStrategy implements OutboxStrategy {
    private final ChatService chatService;

    public DelMiembroEstablecimientoOutboxStrategy(ChatService chatService) {
        this.chatService = chatService;
    }

    public void resolver(Outbox outbox) throws Exception {
        this.chatService.quitarMiembroDelEstablecimiento(outbox.getId().toString());
    }
}
