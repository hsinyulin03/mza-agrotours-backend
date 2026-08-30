package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.notificacion.NotificacionDTO;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notificacion")
public class NotificacionController {
    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> listarNotificaciones(
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        List<NotificacionDTO> dtos = service.listarNotificaciones(usuarioAuthDetails.getEmail(),null);
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @GetMapping("/no-leidas/cantidad")
    public ResponseEntity<?> contarNoLeidas(
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        long cantidad = service.contarNoLeidas(usuarioAuthDetails.getEmail(), null);
        return ResponseEntity.ok(ApiResponse.ok(cantidad));
    }

    @PatchMapping("/{idNotificacion}/leer")
    public ResponseEntity<?> marcarLeida(
            @PathVariable UUID idNotificacion,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        service.marcarLeida(idNotificacion, usuarioAuthDetails.getEmail(), null);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}
