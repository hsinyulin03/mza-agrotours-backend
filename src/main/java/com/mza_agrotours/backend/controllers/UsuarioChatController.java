package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.services.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/usuario/chats")
public class UsuarioChatController {
    private final ChatService chatService;

    public UsuarioChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/iniciar/{establecimientoId}")
    public ResponseEntity<?> iniciarChat(@PathVariable UUID establecimientoId, @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        this.chatService.iniciarChat(establecimientoId, usuarioAuthDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
