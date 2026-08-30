package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.notificacion.TokenNotificacionReqDTO;
import com.mza_agrotours.backend.services.notificaciones.TokenNotificacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notificacion/dispositivos")
public class TokenNotificacionController {

    private final TokenNotificacionService service;

    public TokenNotificacionController(TokenNotificacionService service) {
            this.service = service;
    }

    //se debe llamar cada vez que el usuario se loguee
    @PostMapping("/alta")
    public ResponseEntity<?> registrarDispositivo(
            @Valid @RequestBody TokenNotificacionReqDTO req,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        service.registrarToken(usuarioAuthDetails.getEmail(), req.getToken());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    //se debe llamar cada vez que el usuario cierre sesión
    @PostMapping("/baja")
    public ResponseEntity<?> eliminarDispositivo(
            @Valid @RequestBody TokenNotificacionReqDTO req,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        service.eliminarToken(usuarioAuthDetails.getEmail(), req.getToken());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}

