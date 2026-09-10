package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.notificacion.NotificacionDTO;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/establecimientos/{establecimientoId}/notificacion")
public class NotificacionEstablecimientoController {

    private final NotificacionService service;

    public NotificacionEstablecimientoController(NotificacionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@estAuth.esProductorVigente(authentication, #establecimientoId)")
    public ResponseEntity<?> listar(
            @PathVariable UUID establecimientoId,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        List<NotificacionDTO> dtos = service.listarNotificaciones(email, establecimientoId);
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @GetMapping("/no-leidas/cantidad")
    @PreAuthorize("@estAuth.esProductorVigente(authentication, #establecimientoId)")
    public ResponseEntity<?>  contarNoLeidas(
            @PathVariable UUID establecimientoId,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        long cantidad = service.contarNoLeidas(email, establecimientoId);
        return ResponseEntity.ok(ApiResponse.ok(cantidad));
    }

    @PatchMapping("/{notificacionId}/leer")
    @PreAuthorize("@estAuth.esProductorVigente(authentication, #establecimientoId)")
    public ResponseEntity<?>  marcarLeida(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID notificacionId,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        NotificacionDTO notificacionActualizada = service.marcarLeida(notificacionId, email, establecimientoId);
        return ResponseEntity.ok(ApiResponse.ok(notificacionActualizada));
    }


}
