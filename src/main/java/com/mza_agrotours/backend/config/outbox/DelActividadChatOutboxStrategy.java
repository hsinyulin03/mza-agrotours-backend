package com.mza_agrotours.backend.config.outbox;

import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.services.ChatService;
import org.springframework.stereotype.Component;

@Component
public class DelActividadChatOutboxStrategy implements OutboxStrategy {
    private final ChatService chatService;

    public DelActividadChatOutboxStrategy(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void resolver(Outbox outbox) throws Exception {
        this.chatService.quitarActividad(outbox.getEntidadId());
    }
}
